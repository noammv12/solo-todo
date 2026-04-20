package com.solotodo.data.sync

import com.solotodo.data.auth.AuthRepository
import com.solotodo.data.local.SoloTodoDb
import com.solotodo.data.local.dao.OpLogDao
import com.solotodo.data.local.dao.SyncStateDao
import com.solotodo.data.local.entity.OpLogEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates push (drain op-log → Postgrest) and pull (fetch remote rows
 * since last cursor → upsert into Room silently). Safe to call concurrently —
 * a [Mutex] serialises pushes within a single process.
 *
 * Callers:
 *  - [PushWorker] → [pushOnce]
 *  - [PullWorker] → [pullOnce]
 *  - [PeriodicSyncWorker] → [fullSync]
 *  - [SyncBootstrapper] → [clearLocalIfUserChanged] on auth transitions
 *
 * All public methods short-circuit if no user is signed in.
 */
@Singleton
class SyncEngine @Inject constructor(
    private val db: SoloTodoDb,
    private val opLogDao: OpLogDao,
    private val syncStateDao: SyncStateDao,
    private val authRepository: AuthRepository,
    private val pushTranslator: PushTranslator,
    private val pullTranslator: PullTranslator,
    private val statRecomputer: StatRecomputer,
    private val clock: Clock,
) {

    private val pushLock = Mutex()

    data class PushResult(val pushed: Int, val failed: Int)
    data class PullResult(val perTable: List<PullTranslator.TableResult>) {
        val total: Int get() = perTable.sumOf { it.rows }
    }

    /**
     * In-memory snapshot of the most recent sync attempt. Consumed by the
     * dev-gallery diagnostics panel. Reset to `null` on sign-out via
     * [clearLocalIfUserChanged]'s caller (bootstrapper).
     */
    data class Snapshot(
        val ranAt: Instant,
        val pushResult: PushResult?,
        val pullResult: PullResult?,
        val errorMessage: String?,
    )

    private val _lastSnapshot = MutableStateFlow<Snapshot?>(null)
    val lastSnapshot: StateFlow<Snapshot?> = _lastSnapshot.asStateFlow()

    suspend fun pushOnce(): PushResult = pushLock.withLock {
        val uid = authRepository.currentUserId() ?: return@withLock PushResult(0, 0)
        var pushed = 0
        var failed = 0
        val acked = mutableListOf<String>()

        while (true) {
            val batch = opLogDao.pending(limit = BATCH)
            if (batch.isEmpty()) break

            for (op in batch) {
                try {
                    pushTranslator.push(op, uid)
                    acked += op.opId
                    pushed++
                } catch (e: Exception) {
                    failed++
                    // Record the failure. After QUARANTINE_THRESHOLD retries
                    // the op is stamped with QUARANTINE_SENTINEL so `pending()`
                    // stops returning it and the queue can drain past it.
                    opLogDao.incrementRetry(
                        opId = op.opId,
                        errorMessage = (e.message ?: e::class.simpleName)
                            ?.take(MAX_ERROR_LENGTH),
                        threshold = OpLogEntity.QUARANTINE_THRESHOLD,
                        quarantineSentinel = OpLogEntity.QUARANTINE_SENTINEL,
                    )
                }
            }

            if (acked.isNotEmpty()) {
                opLogDao.markSynced(acked.toList(), clock.now())
                acked.clear()
            }

            // Stop if we only drained unsuccessful ops (all still pending) to
            // avoid busy-looping on a persistent failure.
            if (batch.size == failed) break
        }
        PushResult(pushed, failed)
    }

    suspend fun pullOnce(): PullResult {
        val uid = authRepository.currentUserId() ?: return PullResult(emptyList())
        clearLocalIfUserChanged(uid)
        val perTable = pullTranslator.pullAll(uid)
        statRecomputer.recompute()
        return PullResult(perTable)
    }

    suspend fun fullSync(): Pair<PushResult, PullResult> {
        val push = pushOnce()
        val pull = pullOnce()
        _lastSnapshot.value = Snapshot(
            ranAt = clock.now(),
            pushResult = push,
            pullResult = pull,
            errorMessage = null,
        )
        return push to pull
    }

    /**
     * Debug-gallery action: runs a full sync and captures any exception into
     * [lastSnapshot] so the UI can display it. Unlike [fullSync] this never
     * throws to the caller.
     */
    suspend fun runSyncNow() {
        try {
            fullSync()
        } catch (e: Exception) {
            _lastSnapshot.value = Snapshot(
                ranAt = clock.now(),
                pushResult = null,
                pullResult = null,
                errorMessage = e.message ?: e::class.simpleName,
            )
        }
    }

    /** Debug-gallery action: wipe every op-log entry. */
    suspend fun clearOpLog(): Int = opLogDao.clearAll()

    /**
     * Debug-gallery action: release every quarantined op so it retries on the
     * next drain. Returns the number of rows reset.
     */
    suspend fun releaseQuarantine(): Int =
        opLogDao.releaseQuarantine(OpLogEntity.QUARANTINE_SENTINEL)

    /**
     * If the last-seen user differs from the current one, wipe all local
     * tables (including op-log) before the next pull. Protects against
     * cross-user data leaks after sign-out / sign-in with a different
     * account.
     */
    suspend fun clearLocalIfUserChanged(currentUserId: String) {
        val lastSeen = syncStateDao.anyLastUserId()
        if (lastSeen != null && lastSeen != currentUserId) {
            // Note: clearAllTables() cannot run inside a transaction.
            db.clearAllTables()
        }
    }

    /** Fire-and-forget: called from bootstrapper after writes. Debounced by worker. */
    fun requestExpedited() {
        // No-op here; scheduling happens in [SyncBootstrapper]. This method
        // exists so repositories can later depend on SyncEngine directly
        // without re-plumbing.
    }

    companion object {
        /** Ops per drain batch; tuned for op-log index + Postgrest throughput. */
        const val BATCH = 100

        /** Error messages are truncated before storage to keep op_log rows small. */
        const val MAX_ERROR_LENGTH = 500
    }
}
