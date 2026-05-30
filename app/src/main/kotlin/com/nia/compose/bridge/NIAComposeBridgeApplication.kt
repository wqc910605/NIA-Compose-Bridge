package com.nia.compose.bridge

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class NIAComposeBridgeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.Forest.plant(Timber.DebugTree())
    }
}