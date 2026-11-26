package com.nexus.app.ui.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Icon set drawn on a 24-unit grid with square-capped strokes (no external icon dependency). */
enum class NexusIcon { Home, Courses, Community, Messages, Profile, Bell, Plus, Search, Close, Check, Eye, EyeOff }

private fun DrawScope.poly(s: Float, color: Color, stroke: Stroke, closed: Boolean, vararg xy: Float) {
    val path = Path()
    var i = 0
    while (i + 1 < xy.size) {
        if (i == 0) path.moveTo(xy[i] * s, xy[i + 1] * s) else path.lineTo(xy[i] * s, xy[i + 1] * s)
        i += 2
    }
    if (closed) path.close()
    drawPath(path = path, color = color, style = stroke)
}

private fun DrawScope.ring(s: Float, color: Color, stroke: Stroke, cx: Float, cy: Float, r: Float) {
    drawCircle(color = color, radius = r * s, center = Offset(cx * s, cy * s), style = stroke)
}

@Composable
fun NexusIconView(
    icon: NexusIcon,
    modifier: Modifier = Modifier,
    tint: Color = LocalNexusColors.current.ink,
    size: Dp = 24.dp,
    contentDescription: String? = null
) {
    val base = if (contentDescription != null) {
        modifier.size(size).semantics { this.contentDescription = contentDescription }
    } else {
        modifier.size(size)
    }
    Canvas(modifier = base) {
        val s = this.size.width / 24f
        val stroke = Stroke(width = 1.8f * s, cap = StrokeCap.Square, join = StrokeJoin.Miter)
        when (icon) {
            NexusIcon.Home -> {
                poly(s, tint, stroke, false, 3f, 11f, 12f, 3f, 21f, 11f)
                poly(s, tint, stroke, false, 5f, 9.5f, 5f, 21f, 19f, 21f, 19f, 9.5f)
                poly(s, tint, stroke, false, 10f, 21f, 10f, 14f, 14f, 14f, 14f, 21f)
            }
            NexusIcon.Courses -> {
                poly(s, tint, stroke, true, 5f, 3f, 19f, 3f, 19f, 21f, 5f, 21f)
                poly(s, tint, stroke, false, 9f, 3f, 9f, 21f)
                poly(s, tint, stroke, false, 12f, 8f, 16f, 8f)
                poly(s, tint, stroke, false, 12f, 12f, 16f, 12f)
            }
            NexusIcon.Community -> {
                ring(s, tint, stroke, 9f, 8f, 3.5f)
                poly(s, tint, stroke, false, 3f, 20f, 4f, 15f, 14f, 15f, 15f, 20f)
                ring(s, tint, stroke, 17.5f, 9.5f, 2.5f)
                poly(s, tint, stroke, false, 17f, 15f, 21f, 15f, 21.5f, 19f)
            }
            NexusIcon.Messages -> {
                poly(s, tint, stroke, true, 3f, 4f, 21f, 4f, 21f, 16f, 10f, 16f, 5f, 21f, 5f, 16f, 3f, 16f)
            }
            NexusIcon.Profile -> {
                ring(s, tint, stroke, 12f, 8f, 4f)
                poly(s, tint, stroke, false, 4f, 21f, 5f, 15f, 19f, 15f, 20f, 21f)
            }
            NexusIcon.Bell -> {
                poly(s, tint, stroke, true, 5f, 18f, 7f, 16f, 7f, 10f, 9f, 6f, 15f, 6f, 17f, 10f, 17f, 16f, 19f, 18f)
                poly(s, tint, stroke, false, 10f, 21f, 14f, 21f)
            }
            NexusIcon.Plus -> {
                poly(s, tint, stroke, false, 12f, 5f, 12f, 19f)
                poly(s, tint, stroke, false, 5f, 12f, 19f, 12f)
            }
            NexusIcon.Search -> {
                ring(s, tint, stroke, 10f, 10f, 6f)
                poly(s, tint, stroke, false, 14.5f, 14.5f, 20f, 20f)
            }
            NexusIcon.Close -> {
                poly(s, tint, stroke, false, 5f, 5f, 19f, 19f)
                poly(s, tint, stroke, false, 19f, 5f, 5f, 19f)
            }
            NexusIcon.Check -> {
                poly(s, tint, stroke, false, 4f, 12f, 10f, 18f, 20f, 6f)
            }
            NexusIcon.Eye -> {
                poly(s, tint, stroke, true, 2f, 12f, 7f, 7f, 17f, 7f, 22f, 12f, 17f, 17f, 7f, 17f)
                ring(s, tint, stroke, 12f, 12f, 3f)
            }
            NexusIcon.EyeOff -> {
                poly(s, tint, stroke, true, 2f, 12f, 7f, 7f, 17f, 7f, 22f, 12f, 17f, 17f, 7f, 17f)
                ring(s, tint, stroke, 12f, 12f, 3f)
                poly(s, tint, stroke, false, 4f, 4f, 20f, 20f)
            }
        }
    }
}
