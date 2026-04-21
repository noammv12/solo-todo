package com.solotodo

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.solotodo.core.lifecycle.ForegroundObserver
import com.solotodo.core.notifications.NotificationChannels
import com.solotodo.data.sync.SyncBootstrapper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SoloTodoApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var syncBootstrapper: SyncBootstrapper
    @Inject lateinit var foregroundObserver: ForegroundObserver

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createChannels(this)
        syncBootstrapper.start()
        foregroundObserver.attach()
    }
}
