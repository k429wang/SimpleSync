package com.example.simplesync

import android.app.Application
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class SimpleSyncApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // enable verbose logging
        OneSignal.Debug.logLevel = LogLevel.VERBOSE // TODO: remove for production
        // initialize with OneSignal App ID
        OneSignal.initWithContext(this, "e8b2f952-c48a-4038-bcfa-bf6d92d8a177")
        // request notification permission
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(fallbackToSettings = false)
        }
    }
}
