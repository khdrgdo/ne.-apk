package com.nexus.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nexus.app.R
import com.nexus.app.core.AppContainer
import com.nexus.app.core.session.SessionState
import com.nexus.app.ui.designsystem.LocalNexusColors
import com.nexus.app.ui.designsystem.NexusBottomBar
import com.nexus.app.ui.designsystem.NexusIcon
import com.nexus.app.ui.designsystem.NexusNavItem
import com.nexus.app.ui.feature.auth.AuthRoute
import com.nexus.app.ui.feature.auth.AuthViewModel

/** Gates the UI on the session: blank while initializing, auth when signed out, tabs when active. */
@Composable
fun NexusNavHost(
    container: AppContainer,
    sessionState: SessionState,
    modifier: Modifier = Modifier
) {
    val colors = LocalNexusColors.current
    when (sessionState) {
        is SessionState.Initializing -> {
            Box(modifier = modifier.fillMaxSize().background(colors.paper))
        }
        is SessionState.SignedOut -> {
            val authViewModel: AuthViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { AuthViewModel(container.sessionManager, container.authNotice) }
                }
            )
            AuthRoute(viewModel = authViewModel, modifier = modifier)
        }
        is SessionState.Active -> {
            MainShell(modifier = modifier)
        }
    }
}

@Composable
private fun MainShell(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val items = listOf(
        NexusNavItem(Routes.HOME, stringResource(R.string.nav_home), NexusIcon.Home),
        NexusNavItem(Routes.COURSES, stringResource(R.string.nav_courses), NexusIcon.Courses),
        NexusNavItem(Routes.COMMUNITY, stringResource(R.string.nav_community), NexusIcon.Community),
        NexusNavItem(Routes.MESSAGES, stringResource(R.string.nav_messages), NexusIcon.Messages),
        NexusNavItem(Routes.PROFILE, stringResource(R.string.nav_profile), NexusIcon.Profile)
    )
    Column(modifier = modifier.fillMaxSize().statusBarsPadding()) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.weight(1f)
        ) {
            composable(Routes.HOME) { HomeStub() }
            composable(Routes.COURSES) { CoursesStub() }
            composable(Routes.COMMUNITY) { CommunityStub() }
            composable(Routes.MESSAGES) { MessagesStub() }
            composable(Routes.PROFILE) { ProfileStub() }
        }
        NexusBottomBar(
            items = items,
            selectedRoute = currentRoute,
            onSelect = { target ->
                navController.navigate(target) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}
