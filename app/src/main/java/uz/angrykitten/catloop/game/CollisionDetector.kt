package uz.angrykitten.catloop.game

import kotlin.math.*

/**
 * Pure-math collision utilities.
 *
 * Coordinate system: ring radius = 1.0 (normalised). Centre = (0, 0).
 * Spike angles are ABSOLUTE (already combined with ringAngle).
 */
object CollisionDetector {

    /**
     * Returns true if the cat at normalised position ([catX], [catY]) is
     * within [thresholdDeg] degrees of any ABSOLUTE spike angle when near
     * the ring wall.
     *
     * @param spikes       Absolute angles (degrees).
     * @param thresholdDeg Collision half-cone in degrees.
     */
    fun checkSpikeAtBoundary(
        catX: Float,
        catY: Float,
        spikes: List<Float>,
        thresholdDeg: Float = 9f,
    ): Boolean {
        val dist = sqrt(catX * catX + catY * catY)
        if (dist < 0.90f) return false

        val catAngleDeg = normalizeAngle(
            Math.toDegrees(atan2(catY.toDouble(), catX.toDouble())).toFloat()
        )
        return spikes.any { spikeAngle ->
            angleDiff(catAngleDeg, spikeAngle) < thresholdDeg
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun angleDiff(a: Float, b: Float): Float {
        var d = abs(a - b) % 360f
        if (d > 180f) d = 360f - d
        return d
    }

    fun normalizeAngle(a: Float): Float {
        var r = a % 360f
        if (r < 0f) r += 360f
        return r
    }
}
