package com.nexus.app.ui.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

enum class NexusThemeMode { System, Light, Dark }

private val SquareShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
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
