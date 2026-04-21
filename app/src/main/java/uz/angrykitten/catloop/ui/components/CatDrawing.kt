package uz.angrykitten.catloop.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import uz.angrykitten.catloop.ui.theme.CatLoopColors

/**
 * Draws an angry orange cat face centred at [center].
 *
 * @param center       World-space centre of the cat head.
 * @param radius       Radius of the head circle (pixels).
 * @param facingAngle  Cat's angle on the ring (degrees, 0 = right).
 *                     The drawing is rotated so the cat's top always points
 *                     toward the ring centre (inward).
 */
fun DrawScope.drawCatFace(center: Offset, radius: Float, facingAngle: Float) {
    // Rotate so the cat's top (–Y direction) points inward.
    // When catAngle = 0° (right side of ring), inward is ←, which means
    // we rotate the local frame by (facingAngle + 90°).
    withTransform({
        rotate(degrees = facingAngle + 90f, pivot = center)
    }) {
        internalDrawCatFace(center, radius)
    }
}

// ---------- Internal implementation (unrotated) ----------

private fun DrawScope.internalDrawCatFace(center: Offset, radius: Float) {
    val strokeW   = (radius * 0.075f).coerceAtLeast(2f)
    val darkBrown = CatLoopColors.DarkBrown

    // ── LEFT EAR ──────────────────────────────────────────────────────────────
    val leftEarOuter = Path().apply {
        moveTo(center.x - radius * 0.38f, center.y - radius * 0.72f)
        lineTo(center.x - radius * 0.82f, center.y - radius * 1.48f)
        lineTo(center.x + radius * 0.04f, center.y - radius * 0.94f)
        close()
    }
    drawPath(leftEarOuter, color = CatLoopColors.PrimaryOrange)
    drawPath(leftEarOuter, color = darkBrown, style = Stroke(width = strokeW, join = StrokeJoin.Round))

    // ── RIGHT EAR ─────────────────────────────────────────────────────────────
    val rightEarOuter = Path().apply {
        moveTo(center.x - radius * 0.04f, center.y - radius * 0.94f)
        lineTo(center.x + radius * 0.82f, center.y - radius * 1.48f)
        lineTo(center.x + radius * 0.38f, center.y - radius * 0.72f)
        close()
    }
    drawPath(rightEarOuter, color = CatLoopColors.PrimaryOrange)
    drawPath(rightEarOuter, color = darkBrown, style = Stroke(width = strokeW, join = StrokeJoin.Round))

    // ── INNER EARS (pink) ─────────────────────────────────────────────────────
    val leftEarInner = Path().apply {
        moveTo(center.x - radius * 0.40f, center.y - radius * 0.78f)
        lineTo(center.x - radius * 0.69f, center.y - radius * 1.26f)
        lineTo(center.x - radius * 0.06f, center.y - radius * 0.94f)
        close()
    }
    drawPath(leftEarInner, color = CatLoopColors.LightPink)

    val rightEarInner = Path().apply {
        moveTo(center.x + radius * 0.06f, center.y - radius * 0.94f)
        lineTo(center.x + radius * 0.69f, center.y - radius * 1.26f)
        lineTo(center.x + radius * 0.40f, center.y - radius * 0.78f)
        close()
    }
    drawPath(rightEarInner, color = CatLoopColors.LightPink)

    // ── MAIN FACE CIRCLE ──────────────────────────────────────────────────────
    drawCircle(color = CatLoopColors.PrimaryOrange, radius = radius, center = center)
    drawCircle(color = darkBrown, radius = radius, center = center,
        style = Stroke(width = strokeW))

    // ── TABBY FOREHEAD STRIPES ────────────────────────────────────────────────
    val stripeColor = Color(0xFFD97F00).copy(alpha = 0.55f)
    val stripeW     = radius * 0.07f
    drawLine(stripeColor,
        Offset(center.x - radius * 0.145f, center.y - radius * 0.88f),
        Offset(center.x - radius * 0.10f,  center.y - radius * 0.42f), stripeW)
    drawLine(stripeColor,
        Offset(center.x,  center.y - radius * 0.94f),
        Offset(center.x,  center.y - radius * 0.42f), stripeW)
    drawLine(stripeColor,
        Offset(center.x + radius * 0.145f, center.y - radius * 0.88f),
        Offset(center.x + radius * 0.10f,  center.y - radius * 0.42f), stripeW)

    // ── EYES ─────────────────────────────────────────────────────────────────
    val eyeY      = center.y - radius * 0.12f
    val eyeW      = radius * 0.33f
    val eyeH      = radius * 0.25f
    val leftEyeX  = center.x - radius * 0.39f
    val rightEyeX = center.x + radius * 0.39f

    drawOval(color = CatLoopColors.YellowOrb,
        topLeft = Offset(leftEyeX  - eyeW / 2f, eyeY - eyeH / 2f), size = Size(eyeW, eyeH))
    drawOval(color = CatLoopColors.YellowOrb,
        topLeft = Offset(rightEyeX - eyeW / 2f, eyeY - eyeH / 2f), size = Size(eyeW, eyeH))

    // Black pupils
    val pupilR = radius * 0.10f
    drawCircle(Color.Black, pupilR, Offset(leftEyeX,  eyeY))
    drawCircle(Color.Black, pupilR, Offset(rightEyeX, eyeY))

    // Eye shines
    val shineR = radius * 0.04f
    drawCircle(Color.White, shineR, Offset(leftEyeX  - pupilR * 0.3f, eyeY - pupilR * 0.5f))
    drawCircle(Color.White, shineR, Offset(rightEyeX - pupilR * 0.3f, eyeY - pupilR * 0.5f))

    // ── ANGRY EYEBROWS ────────────────────────────────────────────────────────
    val browW = radius * 0.13f
    drawLine(darkBrown,
        start = Offset(center.x - radius * 0.63f, eyeY - radius * 0.35f),
        end   = Offset(center.x - radius * 0.18f, eyeY - radius * 0.18f),
        strokeWidth = browW, cap = StrokeCap.Round)
    drawLine(darkBrown,
        start = Offset(center.x + radius * 0.18f, eyeY - radius * 0.18f),
        end   = Offset(center.x + radius * 0.63f, eyeY - radius * 0.35f),
        strokeWidth = browW, cap = StrokeCap.Round)

    // ── NOSE ─────────────────────────────────────────────────────────────────
    drawOval(color = Color(0xFFFF8FAB),
        topLeft = Offset(center.x - radius * 0.10f, center.y + radius * 0.10f),
        size    = Size(radius * 0.20f, radius * 0.14f))

    // ── WHISKERS ─────────────────────────────────────────────────────────────
    val wColor = darkBrown.copy(alpha = 0.38f)
    val wW     = radius * 0.032f
    val wY0    = center.y + radius * 0.18f
    val wY1    = center.y + radius * 0.28f
    val wY2    = center.y + radius * 0.36f

    drawLine(wColor, Offset(center.x - radius * 0.14f, wY0), Offset(center.x - radius * 0.95f, wY0 - radius * 0.14f), wW)
    drawLine(wColor, Offset(center.x - radius * 0.14f, wY1), Offset(center.x - radius * 0.95f, wY1), wW)
    drawLine(wColor, Offset(center.x - radius * 0.14f, wY2), Offset(center.x - radius * 0.95f, wY2 + radius * 0.12f), wW)
    drawLine(wColor, Offset(center.x + radius * 0.14f, wY0), Offset(center.x + radius * 0.95f, wY0 - radius * 0.14f), wW)
    drawLine(wColor, Offset(center.x + radius * 0.14f, wY1), Offset(center.x + radius * 0.95f, wY1), wW)
    drawLine(wColor, Offset(center.x + radius * 0.14f, wY2), Offset(center.x + radius * 0.95f, wY2 + radius * 0.12f), wW)
}
