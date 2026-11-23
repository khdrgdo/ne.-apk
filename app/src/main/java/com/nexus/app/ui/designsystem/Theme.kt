package com.nexus.app.ui.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.RectangleShape

enum class NexusThemeMode { System, Light, Dark }

private val SquareShapes = Shapes(
    extraSmall = RectangleShape,
    small = RectangleShape,
    medium = RectangleShape,
    large = RectangleShape,
    extraLarge = RectangleShape
)

@Composable
fun NexusTheme(
    mode: NexusThemeMode = NexusThemeMode.System,
    content: @Composable () -> Unit
) {
    val dark = when (mode) {
        NexusThemeMode.System -> isSystemInDarkTheme()
        NexusThemeMode.Light -> false
        NexusThemeMode.Dark -> true
    }
    val colors = if (dark) DarkNexusColors else LightNexusColors
    CompositionLocalProvider(LocalNexusColors provides colors) {
        MaterialTheme(
            colorScheme = nexusMaterialColorScheme(colors),
            typography = NexusMaterialTypography,
            shapes = SquareShapes
        ) {
            Surface(color = colors.paper, contentColor = colors.ink) {
                ProvideTextStyle(NexusType.Ui) {
                    content()
                }
            }
        }
    }
}
