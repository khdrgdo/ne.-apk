package com.nexus.app.ui.designsystem

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/** Horizontal rule in ink (default 1dp). */
@Composable
fun Rule(
    modifier: Modifier = Modifier,
    thickness: Dp = NexusDims.RuleThin,
    color: androidx.compose.ui.graphics.Color = LocalNexusColors.current.ink
) {
    Box(modifier = modifier.fillMaxWidth().height(thickness).background(color))
}

/** Gazette double rule: 3dp, 3dp gap, 1dp. */
@Composable
fun DoubleRule(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Rule(thickness = NexusDims.RuleThick)
        Spacer(Modifier.height(3.dp))
        Rule(thickness = NexusDims.RuleThin)
    }
}

/** Accent square plus a label. */
@Composable
fun Kicker(label: String, modifier: Modifier = Modifier) {
    val c = LocalNexusColors.current
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(NexusDims.KickerSquare).background(c.accent))
        Spacer(Modifier.width(6.dp))
        Text(text = label, style = NexusType.Tag.copy(fontSize = 11.sp), color = c.accent)
    }
}

/** Title at the start, optional accent link at the end, 2dp ink rule below. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val c = LocalNexusColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(text = title, style = NexusType.SectionTitle, color = c.ink)
            if (actionLabel != null) {
                val clickMod = if (onAction != null) {
                    Modifier.clickable(role = Role.Button, onClick = onAction)
                } else {
                    Modifier
                }
                Text(
                    text = actionLabel,
                    style = NexusType.UiBold,
                    color = c.accent,
                    modifier = clickMod.padding(vertical = 6.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Rule(thickness = NexusDims.RuleMedium)
    }
}

/** Outlined 1dp tag. */
@Composable
fun Tag(text: String, modifier: Modifier = Modifier) {
    val c = LocalNexusColors.current
    Text(
        text = text,
        style = NexusType.Tag,
        color = c.ink,
        modifier = modifier
            .border(NexusDims.RuleThin, c.ink)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

/** Highlighter-style tag. */
@Composable
fun MarkTag(text: String, modifier: Modifier = Modifier) {
    val c = LocalNexusColors.current
    Text(
        text = text,
        style = NexusType.Tag,
        color = c.onMark,
        modifier = modifier
            .background(c.mark)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

enum class NexusButtonStyle { Filled, Outlined, Text }

@Composable
fun NexusButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: NexusButtonStyle = NexusButtonStyle.Filled,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val c = LocalNexusColors.current
    val textColor = when (style) {
        NexusButtonStyle.Filled -> c.onInk
        NexusButtonStyle.Outlined -> c.ink
        NexusButtonStyle.Text -> c.accent
    }
    var box = modifier.heightIn(min = NexusDims.ButtonHeight)
    box = when (style) {
        NexusButtonStyle.Filled -> box.background(c.ink)
        NexusButtonStyle.Outlined -> box.border(NexusDims.RuleThin, c.ink)
        NexusButtonStyle.Text -> box
    }
    box = box
        .alpha(if (enabled) 1f else 0.4f)
        .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick)
        .padding(horizontal = 20.dp, vertical = 10.dp)
    Box(modifier = box, contentAlignment = Alignment.Center) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = textColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, style = NexusType.UiBold, color = textColor, textAlign = TextAlign.Center)
        }
    }
}

/** 56dp accent square with a 6dp hard ink offset shadow. */
@Composable
fun NexusFab(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    icon: NexusIcon = NexusIcon.Plus
) {
    val c = LocalNexusColors.current
    val total = NexusDims.FabSize + NexusDims.FabShadowOffset
    Box(modifier = modifier.size(total)) {
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .size(NexusDims.FabSize)
                .background(c.ink)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(NexusDims.FabSize)
                .background(c.accent)
                .clickable(role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            NexusIconView(
                icon = icon,
                tint = c.onAccent,
                contentDescription = contentDescription
            )
        }
    }
}

/** Soft box with a 3dp accent rule on the start side. */
@Composable
fun QuoteBox(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = LocalNexusColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(c.surfaceSoft)
    ) {
        Box(
            Modifier
                .width(NexusDims.QuoteRule)
                .fillMaxHeight()
                .background(c.accent)
        )
        Column(modifier = Modifier.padding(12.dp), content = content)
    }
}

private fun initialsOf(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    val letters = parts.take(2).joinToString("") { it.substring(0, 1) }
    return if (letters.isEmpty()) "؟" else letters
}

/** Circular avatar with initials fallback. */
@Composable
fun Avatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    imageUrl: String? = null
) {
    val c = LocalNexusColors.current
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(c.surfaceSoft)
            .border(NexusDims.RuleThin, c.ink, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initialsOf(name),
            style = NexusType.UiBold.copy(fontSize = (size.value * 0.36f).sp, lineHeight = (size.value * 0.5f).sp),
            color = c.ink
        )
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: NexusIcon? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val c = LocalNexusColors.current
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.Xl, vertical = Spacing.Xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.Md)
    ) {
        if (icon != null) {
            NexusIconView(icon = icon, tint = c.mute, size = 40.dp)
        }
        Text(text = title, style = NexusType.SectionTitle, color = c.ink, textAlign = TextAlign.Center)
        if (message != null) {
            Text(text = message, style = NexusType.Ui, color = c.mute, textAlign = TextAlign.Center)
        }
        if (actionLabel != null && onAction != null) {
            NexusButton(text = actionLabel, onClick = onAction, style = NexusButtonStyle.Outlined)
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    retryLabel: String,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    val c = LocalNexusColors.current
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.Xl, vertical = Spacing.Xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.Md)
    ) {
        Kicker(label = title ?: "!")
        Text(text = message, style = NexusType.Ui, color = c.ink, textAlign = TextAlign.Center)
        NexusButton(text = retryLabel, onClick = onRetry, style = NexusButtonStyle.Outlined)
    }
}

/** Pulsing placeholder line for loading lists. */
@Composable
fun LoadingSkeletonLine(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f,
    height: Dp = 14.dp
) {
    val c = LocalNexusColors.current
    val transition = rememberInfiniteTransition(label = "skeleton")
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 900), repeatMode = RepeatMode.Reverse),
        label = "skeletonAlpha"
    )
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .alpha(pulse)
            .background(c.surfaceSoft)
    )
}

/** Square chat bubble: mine = ink fill, theirs = outlined. */
@Composable
fun ChatBubble(
    text: String,
    mine: Boolean,
    modifier: Modifier = Modifier,
    time: String? = null
) {
    val c = LocalNexusColors.current
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        val shape = if (mine) {
            Modifier.widthIn(max = 280.dp).background(c.ink)
        } else {
            Modifier.widthIn(max = 280.dp).border(NexusDims.RuleThin, c.ink)
        }
        Column(modifier = shape.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(text = text, style = NexusType.Ui, color = if (mine) c.onInk else c.ink)
            if (time != null) {
                Text(
                    text = time,
                    style = NexusType.Tiny,
                    color = if (mine) c.faint else c.mute,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
