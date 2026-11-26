package com.nexus.app

import android.app.Application
import com.nexus.app.core.AppContainer

class NexusApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
