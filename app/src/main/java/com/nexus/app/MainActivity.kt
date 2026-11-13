package com.nexus.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.nexus.app.ui.NexusApp

class MainActivity : ComponentActivity() {
    private val nexusViewModel: NexusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nexusViewModel.start(intent?.data)
        setContent { NexusApp(nexusViewModel) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        nexusViewModel.handleDeepLink(intent.data)
    }
}
