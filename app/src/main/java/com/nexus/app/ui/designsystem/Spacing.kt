package com.nexus.app.ui.designsystem

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing scale (4dp grid). The design has no rounded corners and no elevation; see [NexusDims]. */
object Spacing {
    val Xxs: Dp = 2.dp
    val Xs: Dp = 4.dp
    val Sm: Dp = 8.dp
    val Md: Dp = 12.dp
    val Lg: Dp = 16.dp
    val Xl: Dp = 24.dp
    val Xxl: Dp = 32.dp
    val Xxxl: Dp = 48.dp

    /** Horizontal screen gutter. */
    val Gutter: Dp = 16.dp

    /** Minimum touch target. */
    val TouchTarget: Dp = 48.dp
}

/** Fixed sizes of Direction A elements. */
object NexusDims {
    val RuleThin: Dp = 1.dp
    val RuleMedium: Dp = 2.dp
    val RuleThick: Dp = 3.dp
    val KickerSquare: Dp = 8.dp
    val FabSize: Dp = 56.dp
    val FabShadowOffset: Dp = 6.dp
    val NavMarkerWidth: Dp = 24.dp
    val NavMarkerHeight: Dp = 3.dp
    val QuoteRule: Dp = 3.dp
    val ButtonHeight: Dp = 48.dp
}
