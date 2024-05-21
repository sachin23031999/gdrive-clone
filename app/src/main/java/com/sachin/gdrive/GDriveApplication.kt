package com.sachin.gdrive

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import androidx.work.Configuration
import com.sachin.gdrive.di.appModule
import com.sachin.gdrive.notification.NotificationManager
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin


/**
 * Main application class for the app.
 */
class GDriveApplication : Application(), Configuration.Provider, KoinComponent {

    private val notificationManager: NotificationManager by inject()
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        loadKoin(getProcessName())
        // To share the file uri to external to open the file.
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        notificationManager.buildChannel()
    }

    private fun loadKoin(processName: String) {
        Log.d(TAG, "Loading koin for process: $processName")
        startKoin {
            androidLogger()
            androidContext(this@GDriveApplication)
            loadKoinModules(getModules())
        }
    }

    private fun getModules() = listOf(
        appModule
    )

    companion object {
        private val TAG = GDriveApplication::class.java.simpleName
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .setMaxSchedulerLimit(20) // Maximum parallel workers
            .build()

}