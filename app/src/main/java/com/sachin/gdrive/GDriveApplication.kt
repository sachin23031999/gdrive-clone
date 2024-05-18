package com.sachin.gdrive

import android.app.Application
import android.util.Log
import com.sachin.gdrive.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin

class GDriveApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        loadKoin(getProcessName())
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
}