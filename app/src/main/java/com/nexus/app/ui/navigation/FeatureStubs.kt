package com.nexus.app.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nexus.app.R
import com.nexus.app.ui.designsystem.EmptyState
import com.nexus.app.ui.designsystem.LocalNexusColors
import com.nexus.app.ui.designsystem.NexusType
import com.nexus.app.ui.designsystem.Rule
import com.nexus.app.ui.designsystem.Spacing

/* Placeholder screens; each is replaced by its feature package later. */

@Composable
private fun StubScreen(title: String, modifier: Modifier = Modifier) {
    val colors = LocalNexusColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.Gutter)
    ) {
        Spacer(Modifier.height(Spacing.Lg))
        Text(text = title, style = NexusType.ScreenTitle, color = colors.ink)
        Spacer(Modifier.height(Spacing.Sm))
        Rule(thickness = 2.dp)
        EmptyState(
            title = stringResource(R.string.stub_title),
            message = stringResource(R.string.stub_message)
        )
    }
}

@Composable
fun HomeStub() {
    StubScreen(title = stringResource(R.string.nav_home))
}

@Composable
fun CoursesStub() {
    StubScreen(title = stringResource(R.string.nav_courses))
}

@Composable
fun CommunityStub() {
    StubScreen(title = stringResource(R.string.nav_community))
}

@Composable
fun MessagesStub() {
    StubScreen(title = stringResource(R.string.nav_messages))
}

@Composable
fun ProfileStub() {
    StubScreen(title = stringResource(R.string.nav_profile))
}
