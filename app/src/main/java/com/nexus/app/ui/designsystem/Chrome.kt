package com.nexus.app.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text

/**
 * Newspaper masthead: title centred, optional avatar (start) and bell (end), optional dateline, double rule.
 * The avatar and bell are shown only when their callbacks are given.
 */
@Composable
fun NexusMasthead(
    title: String,
    modifier: Modifier = Modifier,
    dateline: String? = null,
    avatarName: String? = null,
    avatarUrl: String? = null,
    onAvatarClick: (() -> Unit)? = null,
    onBellClick: (() -> Unit)? = null,
    bellDescription: String? = null,
    hasUnread: Boolean = false
) {
    val c = LocalNexusColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Sm),
            contentAlignment = Alignment.Center
        ) {
            Text(text = title, style = NexusType.Masthead, color = c.ink, textAlign = TextAlign.Center)
            if (onAvatarClick != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable(role = Role.Button, onClick = onAvatarClick)
                ) {
                    Avatar(name = avatarName.orEmpty(), imageUrl = avatarUrl, size = 36.dp)
                }
            }
            if (onBellClick != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(Spacing.TouchTarget)
                        .clickable(role = Role.Button, onClick = onBellClick),
                    contentAlignment = Alignment.Center
                ) {
                    NexusIconView(icon = NexusIcon.Bell, contentDescription = bellDescription)
                    if (hasUnread) {
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 10.dp, end = 10.dp)
                                .size(8.dp)
                                .background(c.accent)
                        )
                    }
                }
            }
        }
        if (dateline != null) {
            Text(
                text = dateline,
                style = NexusType.Meta,
                color = c.mute,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.Sm)
            )
        }
        DoubleRule()
    }
}

data class NexusNavItem(val route: String, val label: String, val icon: NexusIcon, val badge: Int = 0)

/** Bottom navigation: 1dp ink top rule, 24x3dp accent marker above the active item. */
@Composable
fun NexusBottomBar(
    items: List<NexusNavItem>,
    selectedRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalNexusColors.current
    Column(modifier = modifier.fillMaxWidth().background(c.paper)) {
        Rule(thickness = NexusDims.RuleThin)
        Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            for (item in items) {
                val active = item.route == selectedRoute
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(role = Role.Tab) { onSelect(item.route) }
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier
                            .width(NexusDims.NavMarkerWidth)
                            .height(NexusDims.NavMarkerHeight)
                            .background(if (active) c.accent else Color.Transparent)
                    )
                    Spacer(Modifier.height(8.dp))
                    Box {
                        NexusIconView(icon = item.icon, tint = if (active) c.ink else c.mute)
                        if (item.badge > 0) {
                            Box(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .background(c.accent)
                                    .padding(horizontal = 3.dp)
                            ) {
                                Text(
                                    text = if (item.badge > 99) "99+" else item.badge.toString(),
                                    style = NexusType.Tag,
                                    color = c.onAccent
                                )
                            }
                        }
                    }
                    Text(
                        text = item.label,
                        style = if (active) NexusType.Tiny.copy(color = c.ink) else NexusType.Tiny.copy(color = c.mute),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
