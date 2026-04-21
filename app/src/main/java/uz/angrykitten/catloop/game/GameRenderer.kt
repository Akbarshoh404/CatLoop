package uz.angrykitten.catloop.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import uz.angrykitten.catloop.ui.components.drawCatFace
import uz.angrykitten.catloop.ui.components.drawPawPrints
import uz.angrykitten.catloop.ui.theme.CatLoopColors
import kotlin.math.*

// ─────────────────────────────────────────────────────────────────────────────
// All DrawScope helpers used by GameScreen to render a single frame.
// ─────────────────────────────────────────────────────────────────────────────

/** Background fill + subtle paw-print watermark. */
fun DrawScope.drawGameBackground(canvasSize: Size) {
    drawRect(color = CatLoopColors.Background, size = canvasSize)
    drawPawPrints(canvasSize, alpha = 0.045f)
}

// ─────────────────────────────────────────────────────────────────────────────
// Center divider line
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Draws a vertical center divider line from top to bottom.
 * This visually signals that the LEFT side rotates CCW and RIGHT side rotates CW.
 */
fun DrawScope.drawCenterDivider(canvasSize: Size) {
    val cx = canvasSize.width / 2f

    // Subtle dashed-look using two overlaid lines (outer glow + main line)
    drawLine(
        color       = CatLoopColors.DarkRedCircle.copy(alpha = 0.08f),
        start       = Offset(cx, 0f),
        end         = Offset(cx, canvasSize.height),
        strokeWidth = 6f,
    )
    drawLine(
        color       = CatLoopColors.DarkRedCircle.copy(alpha = 0.22f),
        start       = Offset(cx, 0f),
        end         = Offset(cx, canvasSize.height),
        strokeWidth = 2f,
    )
}

/**
 * Draws small left/right rotation hint arrows inside the ring area.
 */
fun DrawScope.drawRotationHints(
    center: Offset,
    ringRadius: Float,
    ringRotDir: Int,
    density: Float,
) {
    val hintY    = center.y + ringRadius * 0.62f
    val hintSize = 14f * density
    val alpha    = 0.18f

    // Left arrow "◁"
    val leftActive  = ringRotDir == -1
    val rightActive = ringRotDir == 1
    val leftAlpha   = if (leftActive)  0.75f else alpha
    val rightAlpha  = if (rightActive) 0.75f else alpha

    val leftX  = center.x - ringRadius * 0.44f
    val rightX = center.x + ringRadius * 0.44f

    // Left chevron
    val leftPath = Path().apply {
        moveTo(leftX + hintSize * 0.5f, hintY - hintSize * 0.6f)
        lineTo(leftX - hintSize * 0.3f, hintY)
        lineTo(leftX + hintSize * 0.5f, hintY + hintSize * 0.6f)
    }
    drawPath(
        leftPath,
        color = CatLoopColors.DarkRedCircle.copy(alpha = leftAlpha),
        style = Stroke(width = 2.5f * density, join = StrokeJoin.Round),
    )

    // Right chevron
    val rightPath = Path().apply {
        moveTo(rightX - hintSize * 0.5f, hintY - hintSize * 0.6f)
        lineTo(rightX + hintSize * 0.3f, hintY)
        lineTo(rightX - hintSize * 0.5f, hintY + hintSize * 0.6f)
    }
    drawPath(
        rightPath,
        color = CatLoopColors.DarkRedCircle.copy(alpha = rightAlpha),
        style = Stroke(width = 2.5f * density, join = StrokeJoin.Round),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Ring
// ─────────────────────────────────────────────────────────────────────────────

/** Draws the circular boundary ring with glow. */
fun DrawScope.drawRing(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
) {
    // Outer glow shadow
    drawCircle(
        color  = CatLoopColors.DarkRedCircle.copy(alpha = 0.18f),
        radius = radius + strokeWidth * 0.7f,
        center = center,
        style  = Stroke(width = strokeWidth * 0.6f),
    )
    // Main ring body
    drawCircle(
        color  = CatLoopColors.DarkRedCircle,
        radius = radius,
        center = center,
        style  = Stroke(width = strokeWidth),
    )
    // Inner gloss line
    drawCircle(
        color  = Color.White.copy(alpha = 0.20f),
        radius = radius - strokeWidth * 0.40f,
        center = center,
        style  = Stroke(width = strokeWidth * 0.12f),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Spikes  (absolute screen-space angles)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Draws one inward-pointing spike triangle at [absoluteSpikeAngle] degrees.
 *
 * @param popScale  0.0 = just spawned (invisible), 1.0 = fully grown.
 */
fun DrawScope.drawSpike(
    center: Offset,
    ringRadius: Float,
    absoluteSpikeAngle: Float,
    spikeSize: Float,
    spikeHeight: Float,
    popScale: Float = 1f,
) {
    if (popScale <= 0.01f) return

    val rad  = Math.toRadians(absoluteSpikeAngle.toDouble())
    val cosA = cos(rad).toFloat()
    val sinA = sin(rad).toFloat()

    // Perpendicular direction (tangent to the ring at this angle)
    val perpX = -sinA
    val perpY =  cosA

    // Spike base — centred on the ring inner edge
    val baseCX = center.x + cosA * ringRadius
    val baseCY = center.y + sinA * ringRadius

    val effHeight = spikeHeight * popScale
    val effHalfW  = (spikeSize / 2f) * popScale.coerceAtLeast(0.25f)

    val baseLeft  = Offset(baseCX + perpX * effHalfW, baseCY + perpY * effHalfW)
    val baseRight = Offset(baseCX - perpX * effHalfW, baseCY - perpY * effHalfW)

    // Tip points inward from the ring
    val tip = Offset(
        center.x + cosA * (ringRadius - effHeight),
        center.y + sinA * (ringRadius - effHeight),
    )

    val path = Path().apply {
        moveTo(tip.x,       tip.y)
        lineTo(baseLeft.x,  baseLeft.y)
        lineTo(baseRight.x, baseRight.y)
        close()
    }

    drawPath(path, color = CatLoopColors.RedAccent.copy(alpha = popScale.coerceIn(0f, 1f)))
    drawPath(
        path,
        color = CatLoopColors.DarkRedCircle.copy(alpha = popScale.coerceIn(0f, 1f)),
        style = Stroke(width = 2.5f, join = StrokeJoin.Round),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Particle trail
// ─────────────────────────────────────────────────────────────────────────────

fun DrawScope.drawParticleTrail(
    center: Offset,
    ringRadius: Float,
    particles: List<Particle>,
    catRadius: Float,
) {
    for (p in particles) {
        val px = center.x + p.x * ringRadius
        val py = center.y + p.y * ringRadius
        val r  = catRadius * 0.50f * p.scale.coerceAtLeast(0f)
        if (r < 0.5f) continue

        drawCircle(
            color  = CatLoopColors.PrimaryOrange.copy(alpha = p.alpha * 0.55f),
            radius = r,
            center = Offset(px, py),
        )
        drawCircle(
            color  = CatLoopColors.YellowOrb.copy(alpha = p.alpha * 0.25f),
            radius = r * 0.45f,
            center = Offset(px, py),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Cat  (free 2-D position; faces direction of velocity)
// ─────────────────────────────────────────────────────────────────────────────

fun DrawScope.drawCat(
    center: Offset,
    ringRadius: Float,
    catNormX: Float,
    catNormY: Float,
    velX: Float,
    velY: Float,
    catSize: Float,
    catScale: Float = 1f,
) {
    val catX = center.x + catNormX * ringRadius
    val catY = center.y + catNormY * ringRadius
    val catCenter = Offset(catX, catY)

    val facingAngle = if (velX == 0f && velY == 0f) 0f
                      else Math.toDegrees(atan2(velY.toDouble(), velX.toDouble())).toFloat()

    if (catScale != 1f) {
        scale(
            scaleX = 1f / catScale.coerceAtLeast(0.6f),
            scaleY = catScale,
            pivot  = catCenter,
        ) {
            drawCatFace(catCenter, catSize, facingAngle)
        }
    } else {
        drawCatFace(catCenter, catSize, facingAngle)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Score HUD
// ─────────────────────────────────────────────────────────────────────────────

fun DrawScope.drawScoreHUD(
    score: Int,
    ringCenter: Offset,
    textMeasurer: TextMeasurer,
) {
    val scoreStr = score.toString()

    val numberStyle = TextStyle(
        fontSize   = 72.sp,
        fontWeight = FontWeight.Black,
        color      = CatLoopColors.DarkRedCircle.copy(alpha = 0.14f),
    )
    val labelStyle = TextStyle(
        fontSize      = 11.sp,
        fontWeight    = FontWeight.Bold,
        color         = CatLoopColors.PrimaryOrange.copy(alpha = 0.38f),
        letterSpacing = 3.sp,
    )

    val numLayout   = textMeasurer.measure(scoreStr, numberStyle)
    val labelLayout = textMeasurer.measure("SCORE", labelStyle)

    val totalHeight = labelLayout.size.height + 4f + numLayout.size.height
    val startY      = ringCenter.y - totalHeight / 2f

    drawText(
        labelLayout,
        topLeft = Offset(ringCenter.x - labelLayout.size.width / 2f, startY),
    )
    drawText(
        numLayout,
        topLeft = Offset(
            ringCenter.x - numLayout.size.width / 2f,
            startY + labelLayout.size.height + 4f,
        ),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Safe zone indicator (first bounce guide)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * On the very first bounce (bounceCount == 0), draws a gentle pulse on the
 * ring to reassure the player the first pass is obstacle-free.
 */
fun DrawScope.drawSafeRingGlow(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
) {
    drawCircle(
        color  = Color(0xFF4CAF50).copy(alpha = 0.15f),
        radius = radius + strokeWidth * 0.4f,
        center = center,
        style  = Stroke(width = strokeWidth * 0.5f),
    )
}
