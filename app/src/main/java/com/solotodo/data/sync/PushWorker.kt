package com.solotodo.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * One-off WorkManager job that drains the op-log. Enqueued by
 * [SyncBootstrapper] after writes (debounced) and on app start.
 *
 * Uses `setExpedited` at enqueue time for sub-minute latency where allowed,
 * with a graceful fallback to regular work. Network-constrained so it won't
 * fail immediately offline.
 */
@HiltWorker
class PushWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncEngine: SyncEngine,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            syncEngine.pushOnce()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val UNIQUE_NAME = "sync.push"
        private const val MAX_ATTEMPTS = 3
    }
}
