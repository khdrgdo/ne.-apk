package com.nexus.app.core

import android.content.Context
import com.nexus.app.BuildConfig
import com.nexus.app.core.network.ApiClient
import com.nexus.app.core.session.SecureSessionStore
import com.nexus.app.core.session.SessionManager
import com.nexus.app.core.session.SharedPrefsAuthFlowStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/** Manual dependency container, created once by [com.nexus.app.NexusApplication]. */
class AppContainer(context: Context) {
    private val appContext: Context = context.applicationContext

    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val apiClient: ApiClient = ApiClient(
        baseUrl = BuildConfig.SUPABASE_URL,
        apiKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    )

    val sessionManager: SessionManager = SessionManager(
        api = apiClient,
        storage = SecureSessionStore(appContext),
        flowStore = SharedPrefsAuthFlowStore(appContext)
    )

    /** Arabic message from a rejected login deep link, shown on the auth screen. */
    val authNotice: MutableStateFlow<String?> = MutableStateFlow(null)

    /** Reads the stored session once per process; the session state leaves Initializing when it completes. */
    val restoreJob: Job = applicationScope.launch { sessionManager.restore() }
}
