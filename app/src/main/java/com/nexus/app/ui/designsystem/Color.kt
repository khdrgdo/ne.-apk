package com.nexus.app.ui.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Direction A (editorial gazette) palette. Read it with `LocalNexusColors.current`. */
@Immutable
class NexusColors(
    val isDark: Boolean,
    /** Page background (paper). */
    val paper: Color,
    /** Primary text and rules. */
    val ink: Color,
    /** Text on [ink] fills. */
    val onInk: Color,
    /** Secondary text. */
    val mute: Color,
    /** Hairlines and disabled strokes. */
    val faint: Color,
    /** Single accent (links, kicker, active marker, FAB). */
    val accent: Color,
    /** Text on [accent] fills. */
    val onAccent: Color,
    /** Highlighter background for mark tags. */
    val mark: Color,
    /** Text on [mark] fills. */
    val onMark: Color,
    /** Soft surface for quote boxes and skeletons. */
    val surfaceSoft: Color
)

val LightNexusColors = NexusColors(
    isDark = false,
    paper = Color(0xFFF3EEE3),
    ink = Color(0xFF15120E),
    onInk = Color(0xFFF3EEE3),
    mute = Color(0xFF6B6458),
    faint = Color(0xFFCFC6B2),
    accent = Color(0xFFC23B22),
    onAccent = Color(0xFFFFFFFF),
    mark = Color(0xFFE9D27A),
    onMark = Color(0xFF15120E),
    surfaceSoft = Color(0xFFEAE1CE)
)

val DarkNexusColors = NexusColors(
    isDark = true,
    paper = Color(0xFF141210),
    ink = Color(0xFFEFE8D8),
    onInk = Color(0xFF141210),
    mute = Color(0xFFA59D8C),
    faint = Color(0xFF3A352C),
    accent = Color(0xFFE2603F),
    onAccent = Color(0xFF141210),
    mark = Color(0xFFC9AE4A),
    onMark = Color(0xFF141210),
    surfaceSoft = Color(0xFF1E1B16)
)

val LocalNexusColors = staticCompositionLocalOf { LightNexusColors }

/** Material 3 scheme derived from the Nexus palette so stock widgets (text selection, progress) match. */
internal fun nexusMaterialColorScheme(c: NexusColors): ColorScheme =
    if (c.isDark) {
        darkColorScheme(
            primary = c.ink,
            onPrimary = c.onInk,
            secondary = c.accent,
            onSecondary = c.onAccent,
            tertiary = c.mark,
            onTertiary = c.onMark,
            background = c.paper,
            onBackground = c.ink,
            surface = c.paper,
            onSurface = c.ink,
            surfaceVariant = c.surfaceSoft,
            onSurfaceVariant = c.mute,
            outline = c.mute,
            outlineVariant = c.faint,
            error = c.accent,
            onError = c.onAccent
        )
    } else {
        lightColorScheme(
            primary = c.ink,
            onPrimary = c.onInk,
            secondary = c.accent,
            onSecondary = c.onAccent,
            tertiary = c.mark,
            onTertiary = c.onMark,
            background = c.paper,
            onBackground = c.ink,
            surface = c.paper,
            onSurface = c.ink,
            surfaceVariant = c.surfaceSoft,
            onSurfaceVariant = c.mute,
            outline = c.mute,
            outlineVariant = c.faint,
            error = c.accent,
            onError = c.onAccent
        )
    }
