package uz.angrykitten.catloop.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Draws 8 faint paw-print silhouettes scattered around the canvas.
 * Used as a decorative background on menu and game screens.
 */
fun DrawScope.drawPawPrints(canvasSize: Size, alpha: Float = 0.06f) {
    val color = Color(0xFF3E1F00).copy(alpha = alpha)
    val s     = canvasSize

    val paws = listOf(
        Triple(s.width * 0.10f, s.height * 0.13f, -20f),
        Triple(s.width * 0.86f, s.height * 0.09f,  30f),
        Triple(s.width * 0.05f, s.height * 0.50f,  10f),
        Triple(s.width * 0.91f, s.height * 0.45f, -15f),
        Triple(s.width * 0.20f, s.height * 0.82f,  25f),
        Triple(s.width * 0.76f, s.height * 0.86f, -30f),
        Triple(s.width * 0.50f, s.height * 0.93f,   5f),
        Triple(s.width * 0.62f, s.height * 0.04f, -10f),
    )
    val pawSize = s.width * 0.075f
    paws.forEach { (x, y, rot) ->
        drawSinglePaw(Offset(x, y), pawSize, rot, color)
    }
}

private fun DrawScope.drawSinglePaw(center: Offset, size: Float, rotationDeg: Float, color: Color) {
    // We manually rotate each component since withTransform works relative to whole canvas.
    // For simplicity, bake the rotation into the math (only small angles, ~±30°).
    val rad   = Math.toRadians(rotationDeg.toDouble()).toFloat()
    val cosR  = kotlin.math.cos(rad)
    val sinR  = kotlin.math.sin(rad)

    fun rot(dx: Float, dy: Float): Offset {
        return Offset(
            center.x + dx * cosR - dy * sinR,
            center.y + dx * sinR + dy * cosR
        )
    }

    // Main pad (large rounded oval at bottom)
    val padW = size * 0.56f
    val padH = size * 0.46f
    val padC = rot(0f, size * 0.18f)
    drawOval(color, topLeft = Offset(padC.x - padW / 2f, padC.y - padH / 2f), size = Size(padW, padH))

    // 4 toe pads arranged in an arc above
    val toeR  = size * 0.165f
    val toeH  = size * 0.12f
    val toeRW = toeR * 2f
    val toeRH = toeH * 2f

    for (i in 0..3) {
        val fraction = (i / 3f) - 0.5f     // -0.5 … +0.5
        val tx = fraction * size * 0.66f
        val ty = -size * 0.22f - (1f - (fraction * fraction * 4f)) * size * 0.06f
        val tc = rot(tx, ty)
        drawOval(color, topLeft = Offset(tc.x - toeRW / 2f, tc.y - toeRH / 2f),
            size = Size(toeRW, toeRH))
    }
}
