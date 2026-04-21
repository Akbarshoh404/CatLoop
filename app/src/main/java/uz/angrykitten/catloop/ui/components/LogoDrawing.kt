package uz.angrykitten.catloop.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import uz.angrykitten.catloop.ui.theme.CatLoopColors

/**
 * Draws the CatLoop sticker-style logo: an angry cat face with a red heart below.
 *
 * @param center  Centre-point of the entire composition.
 * @param size    Diameter of the cat's head circle. Total height ≈ size * 1.8.
 */
fun DrawScope.drawCatLogo(center: Offset, size: Float) {
    val radius     = size * 0.5f
    val catCenter  = Offset(center.x, center.y - size * 0.12f)
    val heartCy    = center.y + radius + size * 0.18f
    val heartSize  = size * 0.22f

    // ── WHITE STICKER BORDER AROUND CAT CIRCLE ────────────────────────────────
    drawCircle(color = Color.White, radius = radius + size * 0.07f, center = catCenter)

    // ── HEART (drawn first so cat doesn't go over it) ─────────────────────────
    val heartPath    = buildHeartPath(Offset(center.x, heartCy), heartSize)
    val bigHeartPath = buildHeartPath(Offset(center.x, heartCy + size * 0.005f), heartSize + size * 0.05f)

    drawPath(bigHeartPath, color = Color.White)                    // white sticker border
    drawPath(heartPath,    color = CatLoopColors.RedAccent)        // red fill
    drawPath(heartPath,    color = CatLoopColors.DarkBrown,        // outline
        style = Stroke(width = size * 0.018f, join = StrokeJoin.Round))

    // Heart gloss highlight
    drawCircle(
        color  = Color.White.copy(alpha = 0.45f),
        radius = heartSize * 0.28f,
        center = Offset(center.x - heartSize * 0.3f, heartCy - heartSize * 0.35f)
    )

    // ── CAT FACE ─────────────────────────────────────────────────────────────
    drawCatFace(catCenter, radius, facingAngle = 270f)
}

/** Constructs a heart shape using cubic Bézier curves. */
private fun buildHeartPath(center: Offset, size: Float): Path {
    val x = center.x
    val y = center.y
    return Path().apply {
        // Start at the bottom tip
        moveTo(x, y + size * 0.38f)

        // Left half
        cubicTo(
            x - size * 1.00f, y + size * 0.10f,
            x - size * 1.05f, y - size * 0.50f,
            x - size * 0.50f, y - size * 0.72f
        )
        // Left → top centre
        cubicTo(
            x - size * 0.20f, y - size * 0.95f,
            x,                y - size * 0.72f,
            x,                y - size * 0.50f
        )
        // Top centre → right
        cubicTo(
            x,                y - size * 0.72f,
            x + size * 0.20f, y - size * 0.95f,
            x + size * 0.50f, y - size * 0.72f
        )
        // Right half
        cubicTo(
            x + size * 1.05f, y - size * 0.50f,
            x + size * 1.00f, y + size * 0.10f,
            x,                y + size * 0.38f
        )
        close()
    }
}
