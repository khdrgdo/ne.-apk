package com.nexus.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.nexus.app.core.AppContainer
import com.nexus.app.core.session.DeepLinkResult
import com.nexus.app.core.session.SessionState
import com.nexus.app.ui.designsystem.NexusTheme
import com.nexus.app.ui.navigation.NexusNavHost
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        container = (application as NexusApplication).container
        splash.setKeepOnScreenCondition {
            container.sessionManager.state.value is SessionState.Initializing
        }
        if (savedInstanceState == null) {
            handleDeepLink(intent?.data)
        }
        setContent {
            NexusTheme {
                val sessionState by container.sessionManager.state.collectAsStateWithLifecycle()
                NexusNavHost(container = container, sessionState = sessionState)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent.data)
    }

    /** Login callback links are validated by the SessionManager; a rejection is shown on the auth screen. */
    private fun handleDeepLink(uri: Uri?) {
        if (uri == null) return
        lifecycleScope.launch {
            container.restoreJob.join()
            when (val result = container.sessionManager.acceptDeepLink(uri)) {
                is DeepLinkResult.Rejected -> container.authNotice.value = result.message
                is DeepLinkResult.Accepted -> container.authNotice.value = null
                is DeepLinkResult.NotHandled -> Unit
            }
        }
    }
}
