package com.amitshilo.menudeldia

import android.app.Application
import timber.log.Timber

class MenuDelDiaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (com.amitshilo.menudeldia.BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
