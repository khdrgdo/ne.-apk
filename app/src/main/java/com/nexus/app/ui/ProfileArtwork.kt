package com.nexus.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

private data class ArtworkPalette(val base: Color, val accent: Color, val secondary: Color, val line: Color)

@Composable
fun ProfileArtwork(theme: String, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val palette = remember(theme) {
        when (theme) {
            "clouds" -> ArtworkPalette(Color(0xFF123B4A), Color(0xFF9EC7C0), Color(0xFF4E8790), Color(0xFFB6D5D0))
            "moon" -> ArtworkPalette(Color(0xFF28213F), Color(0xFFC7B8E7), Color(0xFF655B89), Color(0xFF9B8DB8))
            "sun" -> ArtworkPalette(Color(0xFF563126), Color(0xFFE2B978), Color(0xFF9A5C3E), Color(0xFFE8CFA8))
            "galaxy" -> ArtworkPalette(Color(0xFF17233E), Color(0xFFB8A2E8), Color(0xFF4E709F), Color(0xFF8C9FC6))
            else -> ArtworkPalette(Color(0xFF1D2B3D), Color(0xFFD2B477), Color(0xFF52657B), Color(0xFF9BA9B6))
        }
    }
    Box(modifier.clip(RoundedCornerShape(18.dp))) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(palette.base)
            // Editorial diagonal field and a quiet technical grid.
            for (i in 0..8) {
                val x = w * (0.42f + i * 0.075f)
                drawLine(palette.line.copy(alpha = .10f), Offset(x, 0f), Offset(x - h * .55f, h), strokeWidth = 1.dp.toPx())
            }
            for (i in 0..4) {
                val y = h * (0.18f + i * .18f)
                drawLine(palette.line.copy(alpha = .08f), Offset(w * .36f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
            }
            when (theme) {
                "moon" -> {
                    val center = Offset(w * .78f, h * .43f)
                    drawCircle(palette.accent.copy(alpha = .16f), radius = h * .40f, center = center)
                    drawCircle(palette.accent.copy(alpha = .78f), radius = h * .29f, center = center)
                    drawCircle(palette.base, radius = h * .25f, center = Offset(center.x + h * .13f, center.y - h * .07f))
                    drawArc(palette.line.copy(alpha = .48f), 200f, 150f, false, topLeft = Offset(w * .47f, h * .08f), size = Size(h * .9f, h * .9f), style = Stroke(1.2.dp.toPx()))
                }
                "sun" -> {
                    val center = Offset(w * .78f, h * .52f)
                    for (i in 0 until 18) {
                        val angle = Math.toRadians((i * 20.0))
                        val r1 = h * .23f; val r2 = h * .43f
                        drawLine(palette.accent.copy(alpha = .58f), Offset(center.x + kotlin.math.cos(angle).toFloat() * r1, center.y + kotlin.math.sin(angle).toFloat() * r1), Offset(center.x + kotlin.math.cos(angle).toFloat() * r2, center.y + kotlin.math.sin(angle).toFloat() * r2), strokeWidth = 1.4.dp.toPx())
                    }
                    drawCircle(palette.accent.copy(alpha = .86f), radius = h * .19f, center = center)
                }
                "clouds" -> {
                    for (i in 0..4) {
                        val inset = i * h * .10f
                        drawArc(palette.accent.copy(alpha = .45f - i * .055f), 190f, 155f, false, topLeft = Offset(w * .43f + inset * .3f, h * .02f + inset), size = Size(h * 1.3f, h * 1.3f), style = Stroke(1.4.dp.toPx()))
                    }
                    drawCircle(palette.accent.copy(alpha = .65f), radius = h * .045f, center = Offset(w * .82f, h * .28f))
                    drawCircle(palette.line.copy(alpha = .35f), radius = h * .025f, center = Offset(w * .66f, h * .72f))
                }
                "galaxy" -> {
                    val center = Offset(w * .76f, h * .52f)
                    drawCircle(palette.accent.copy(alpha = .12f), radius = h * .34f, center = center)
                    drawCircle(palette.accent.copy(alpha = .85f), radius = h * .07f, center = center)
                    drawOval(palette.line.copy(alpha = .72f), topLeft = Offset(center.x - h * .48f, center.y - h * .17f), size = Size(h * .96f, h * .34f), style = Stroke(1.3.dp.toPx()))
                    drawOval(palette.secondary.copy(alpha = .85f), topLeft = Offset(center.x - h * .40f, center.y - h * .30f), size = Size(h * .80f, h * .60f), style = Stroke(1.dp.toPx()))
                    drawCircle(palette.line, radius = h * .025f, center = Offset(center.x + h * .39f, center.y - h * .12f))
                }
                else -> {
                    val center = Offset(w * .78f, h * .50f)
                    for (i in 0..3) drawCircle(palette.accent.copy(alpha = .22f - i * .035f), radius = h * (.16f + i * .11f), center = center, style = Stroke(1.2.dp.toPx()))
                    for (i in 0..14) {
                        val x = w * (.47f + (i % 5) * .10f)
                        val y = h * (.15f + (i / 5) * .30f)
                        drawCircle(palette.line.copy(alpha = .58f), radius = 1.5.dp.toPx(), center = Offset(x, y))
                    }
                }
            }
            drawLine(palette.accent.copy(alpha = .82f), Offset(w * .06f, h * .86f), Offset(w * .33f, h * .86f), strokeWidth = 2.dp.toPx())
        }
        content()
    }
}
