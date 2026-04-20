package com.solotodo.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.solotodo.data.auth.AuthRepository
import com.solotodo.data.local.dao.OpLogDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Boots the sync engine at app start. Lifecycle-less; call [start] once from
 * [com.solotodo.SoloTodoApp.onCreate].
 *
 * Responsibilities:
 *  - Observe [AuthRepository.state]; on Guest/Authenticated enqueue the
 *    initial pull + periodic sync; on NotAuthed cancel both.
 *  - Observe [OpLogDao.observePendingCount], debounced, enqueue a one-off
 *    [PushWorker] when pending ops exist.
 */
@Singleton
class SyncBootstrapper @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val authRepository: AuthRepository,
    private val opLogDao: OpLogDao,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        authRepository.state
            .distinctUntilChanged()
            .onEach { state ->
                when (state) {
                    is AuthRepository.AuthState.Guest,
                    is AuthRepository.AuthState.Authenticated -> {
                        enqueueInitialPull()
                        enqueuePeriodicSync()
                    }
                    AuthRepository.AuthState.NotAuthed -> cancelAll()
                    AuthRepository.AuthState.Loading -> Unit
                }
            }
            .launchIn(scope)

        opLogDao.observePendingCount()
            .debounce(PUSH_DEBOUNCE_MS)
            .distinctUntilChanged()
            .onEach { count -> if (count > 0) enqueuePush() }
            .launchIn(scope)
    }

    private fun enqueuePush() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<PushWorker>()
            .setConstraints(constraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(appContext).enqueueUniqueWork(
            PushWorker.UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    private fun enqueueInitialPull() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<PullWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(appContext).enqueueUniqueWork(
            PullWorker.UNIQUE_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    private fun enqueuePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        val request = PeriodicWorkRequestBuilder<PeriodicSyncWorker>(
            PERIODIC_INTERVAL_MINUTES, TimeUnit.MINUTES,
        )
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            PeriodicSyncWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private fun cancelAll() {
        val wm = WorkManager.getInstance(appContext)
        wm.cancelUniqueWork(PushWorker.UNIQUE_NAME)
        wm.cancelUniqueWork(PullWorker.UNIQUE_NAME)
        wm.cancelUniqueWork(PeriodicSyncWorker.UNIQUE_NAME)
    }

    companion object {
        private const val PUSH_DEBOUNCE_MS = 2_000L
        private const val PERIODIC_INTERVAL_MINUTES = 15L
    }
}
