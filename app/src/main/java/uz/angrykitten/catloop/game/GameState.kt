package uz.angrykitten.catloop.game

/**
 * Complete runtime state of the CatLoop game.
 *
 * Mechanic:
 *  - Cat starts at center, moves outward and bounces off the ring.
 *  - First bounce: no obstacles (safe). Each subsequent bounce spawns a NEW
 *    random set of obstacles — completely replacing the old ones.
 *  - Tapping the LEFT half of the screen rotates the ring counter-clockwise.
 *  - Tapping the RIGHT half rotates the ring clockwise.
 *  - Cat direction is NEVER changed by taps.
 *  - Hitting an obstacle = game over.
 *  - The cat's bounce direction includes random variation so movement is never repetitive.
 *
 * The center divider line is drawn to signal left/right control zones.
 */

/** A single tail-trail particle (normalized coordinates). */
data class Particle(
    val x: Float,
    val y: Float,
    val alpha: Float,
    val scale: Float,
)

data class GameState(
    // ── Cat physics ──────────────────────────────────────────────────────────
    val catX: Float = 0f,
    val catY: Float = 0f,
    val velX: Float = 0f,
    val velY: Float = 0f,
    /** Speed magnitude in ring-radii per second. */
    val catSpeed: Float = 1.1f,

    // ── Ring rotation (tap control) ───────────────────────────────────────────
    /** Current ring rotation angle in degrees. Offsets all spike positions. */
    val ringAngle: Float = 0f,
    /**
     * +1 = clockwise rotation active (right side tapped/held),
     * -1 = counter-clockwise (left side), 0 = no rotation.
     */
    val ringRotDir: Int = 0,
    /** Ring rotation speed in degrees/second. */
    val ringRotSpeed: Float = 90f,

    // ── Squash & stretch ─────────────────────────────────────────────────────
    val catScale: Float = 1f,
    val squashTimer: Float = 0f,

    // ── Spikes ───────────────────────────────────────────────────────────────
    /**
     * RELATIVE spike angles (degrees).
     * Absolute screen angle = normalizeAngle(relAngle + ringAngle).
     */
    val spikes: List<Float> = emptyList(),
    /** Pop-in progress for each spike [0→1]. Same size as [spikes]. */
    val spikeScales: List<Float> = emptyList(),

    // ── Bounce state ─────────────────────────────────────────────────────────
    /** How many times the cat has bounced off the ring. */
    val bounceCount: Int = 0,

    // ── Scoring ───────────────────────────────────────────────────────────────
    val score: Int = 0,

    // ── Game lifecycle ────────────────────────────────────────────────────────
    val isRunning: Boolean = false,
    val gameOver: Boolean = false,
    val isPaused: Boolean = false,

    // ── Difficulty ────────────────────────────────────────────────────────────
    val nextDifficultyScore: Int = 3,

    // ── Visual effects ────────────────────────────────────────────────────────
    val particles: List<Particle> = emptyList(),
)
