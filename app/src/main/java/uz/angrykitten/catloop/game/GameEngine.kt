package uz.angrykitten.catloop.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uz.angrykitten.catloop.audio.AppAudioController
import uz.angrykitten.catloop.data.HighScoreRepository
import kotlin.math.*
import kotlin.random.Random

/**
 * Game ViewModel — drives the CatLoop ricochet + ring-rotation mechanic.
 *
 * MECHANICS:
 *   1. The cat starts at the ring center and moves in a random direction.
 *   2. It bounces off the ring boundary wall (physics reflection + random offset).
 *   3. FIRST bounce: ring has NO obstacles (safe pass).
 *      SUBSEQUENT bounces: a fresh random set of obstacles replaces the old ones.
 *   4. Tapping the LEFT half of the screen sets ring rotation to counter-clockwise.
 *      Tapping the RIGHT half sets ring rotation to clockwise.
 *      Releasing (pointer up) stops the rotation.
 *   5. Tapping NEVER affects the cat's direction.
 *   6. If the cat hits an obstacle at the ring wall, game over.
 *   7. The cat's direction varies after each bounce (random perturbation) so no two
 *      games follow the same path.
 *
 * COORDINATE SYSTEM:
 *   - Origin (0,0) = ring center.
 *   - Ring boundary = distance 1.0 from origin.
 *   - Spikes stored as RELATIVE angles; absolute = relAngle + ringAngle.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HighScoreRepository(application)
    private val audioController = AppAudioController(application)

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState

    private val _isMusicEnabled = MutableStateFlow(true)
    val isMusicEnabled: StateFlow<Boolean> = _isMusicEnabled

    val highScore: Flow<Int> = repository.highScore

    private companion object {
        /** Number of spikes to spawn on each bounce after the first. */
        const val SPIKES_PER_BOUNCE   = 5
        /** Safe arc around the cat's hit point where no spike will spawn. */
        const val SAFE_ZONE_DEG       = 55f
        /** Minimum gap between any two spikes. */
        const val MIN_SPIKE_GAP_DEG   = 22f
        /** Half-width of a spike's collision cone (degrees). */
        const val COLLISION_CONE_DEG  = 9f

        const val MAX_SPIKES          = 12

        const val SQUASH_SCALE        = 1.35f
        const val SQUASH_MS           = 200f

        const val MAX_PARTICLES       = 28
        const val PARTICLE_LIFE_MS    = 400f

        const val SPIKE_POP_MS        = 260f
        const val RING_EDGE           = 1.0f
        const val PUSHBACK            = 0.960f

        /**
         * Maximum random angular perturbation added to the reflected velocity
         * direction after each bounce (degrees). Ensures no repetitive patterns.
         */
        const val BOUNCE_JITTER_DEG   = 35f
    }

    // ── Public API ────────────────────────────────────────────────────────────

    fun startGame() {
        val angleDeg = Random.nextFloat() * 360f
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val speed    = GameState().catSpeed

        _gameState.value = GameState(
            velX      = (cos(angleRad) * speed).toFloat(),
            velY      = (sin(angleRad) * speed).toFloat(),
            spikes    = emptyList(),   // NO obstacles at start
            spikeScales = emptyList(),
            isRunning = true,
            isPaused  = false,
            bounceCount = 0,
        )
    }

    /**
     * Called when the player presses (holds) a side.
     * [isRightSide] = true → clockwise (+1); false → counter-clockwise (-1).
     */
    fun onPressDown(isRightSide: Boolean) {
        val s = _gameState.value
        if (s.gameOver || !s.isRunning || s.isPaused) return
        _gameState.value = s.copy(
            ringRotDir = if (isRightSide) 1 else -1,
        )
    }

    /** Called when the player lifts their finger — stops ring rotation. */
    fun onPressUp() {
        val s = _gameState.value
        if (s.gameOver || !s.isRunning) return
        _gameState.value = s.copy(ringRotDir = 0)
    }

    fun pauseGame() {
        val s = _gameState.value
        if (!s.isRunning || s.gameOver) return
        _gameState.value = s.copy(isPaused = true, ringRotDir = 0)
    }

    fun resumeGame() {
        val s = _gameState.value
        if (!s.isRunning || s.gameOver) return
        _gameState.value = s.copy(isPaused = false)
    }

    fun playMenuMusic() {
        audioController.playMenuMusic()
    }

    fun playGameMusic() {
        audioController.playGameMusic()
    }

    fun stopMusic() {
        audioController.stopMusic()
    }

    fun setMusicEnabled(enabled: Boolean) {
        _isMusicEnabled.value = enabled
        audioController.setMusicEnabled(enabled)
    }

    // ── Game loop ─────────────────────────────────────────────────────────────

    fun update(deltaMs: Float) {
        val s = _gameState.value
        if (s.gameOver || !s.isRunning || s.isPaused) return

        val dt = deltaMs / 1000f

        // ── Ring rotation ────────────────────────────────────────────────────
        val newRingAngle = normalizeAngle(
            s.ringAngle + s.ringRotDir * s.ringRotSpeed * dt
        )

        // ── Cat movement ─────────────────────────────────────────────────────
        var newX  = s.catX + s.velX * dt
        var newY  = s.catY + s.velY * dt
        var velX  = s.velX
        var velY  = s.velY
        var score      = s.score
        var speed      = s.catSpeed
        var rotSpeed   = s.ringRotSpeed
        var spikes     = s.spikes
        var bounceCount = s.bounceCount
        var gameOver   = false
        var catScale    = s.catScale
        var squashTimer = s.squashTimer
        var nextDiff    = s.nextDifficultyScore

        // Advance spike pop-in animations
        val spikeScales = s.spikeScales.map { sv ->
            min(sv + deltaMs / SPIKE_POP_MS, 1f)
        }.toMutableList()

        // ── Boundary / ricochet ──────────────────────────────────────────────
        val dist = sqrt(newX * newX + newY * newY)
        if (dist >= RING_EDGE) {
            val hitAbsDeg = normalizeAngle(
                Math.toDegrees(atan2(newY.toDouble(), newX.toDouble())).toFloat()
            )

            // Check collision with obstacles (only if there are any obstacles)
            val hitSpike = spikes.isNotEmpty() && spikes.any { relAngle ->
                val absAngle = normalizeAngle(relAngle + newRingAngle)
                angleDiff(hitAbsDeg, absAngle) < COLLISION_CONE_DEG
            }

            if (hitSpike) {
                gameOver = true
                audioController.playGameOverSound()
            } else {
                // Physics reflection off circle tangent
                val nx  = newX / dist
                val ny  = newY / dist
                val dot = velX * nx + velY * ny
                velX   -= 2f * dot * nx
                velY   -= 2f * dot * ny

                // ── Random direction perturbation so patterns never repeat ───
                val jitterRad = Math.toRadians(
                    (Random.nextFloat() * 2f - 1f) * BOUNCE_JITTER_DEG.toDouble()
                ).toFloat()
                val cosJ = cos(jitterRad)
                val sinJ = sin(jitterRad)
                val jVelX = velX * cosJ - velY * sinJ
                val jVelY = velX * sinJ + velY * cosJ
                velX = jVelX
                velY = jVelY

                // Re-normalise to preserve speed
                val mag = sqrt(velX * velX + velY * velY)
                if (mag > 1e-6f) {
                    velX = velX / mag * speed
                    velY = velY / mag * speed
                }

                newX = nx * PUSHBACK
                newY = ny * PUSHBACK

                bounceCount++
                score++
                catScale    = SQUASH_SCALE
                squashTimer = SQUASH_MS
                audioController.playBounceSound()

                // ── Difficulty scaling ────────────────────────────────────────
                if (score >= nextDiff) {
                    speed    = (speed    + 0.06f).coerceAtMost(2.0f)
                    rotSpeed = (rotSpeed + 6.0f).coerceAtMost(200f)
                    nextDiff += 3
                }

                // ── Spawn NEW set of obstacles on every bounce from the 1st ──
                // From bounce #1 onwards, replace with a fresh random set.
                if (bounceCount >= 1) {
                    val spikeCount = calculateSpikeCount(bounceCount, score)
                    val avoidRel   = normalizeAngle(hitAbsDeg - newRingAngle)
                    val newSpikes  = generateSpikes(spikeCount, avoidRel)
                    spikes = newSpikes
                    spikeScales.clear()
                    spikeScales.addAll(List(newSpikes.size) { 0f })
                }
            }
        }

        // ── Squash / stretch decay ────────────────────────────────────────────
        if (squashTimer > 0f) {
            squashTimer = (squashTimer - deltaMs).coerceAtLeast(0f)
            val t = squashTimer / SQUASH_MS
            catScale = 1f + (SQUASH_SCALE - 1f) * t * t
        } else {
            catScale = 1f
        }

        // ── Particle trail ────────────────────────────────────────────────────
        val particles = s.particles
            .map { p ->
                p.copy(
                    alpha = (p.alpha - deltaMs / PARTICLE_LIFE_MS).coerceAtLeast(0f),
                    scale = (p.scale - deltaMs / PARTICLE_LIFE_MS * 0.5f).coerceAtLeast(0f),
                )
            }
            .filter { it.alpha > 0.02f }
            .takeLast(MAX_PARTICLES - 1) +
            Particle(x = s.catX, y = s.catY, alpha = 0.75f, scale = 1f)

        if (gameOver) {
            viewModelScope.launch { repository.saveHighScore(score) }
        }

        _gameState.value = s.copy(
            catX                = newX,
            catY                = newY,
            velX                = velX,
            velY                = velY,
            catSpeed            = speed,
            ringAngle           = newRingAngle,
            ringRotSpeed        = rotSpeed,
            catScale            = catScale,
            squashTimer         = squashTimer,
            spikes              = spikes,
            spikeScales         = spikeScales,
            bounceCount         = bounceCount,
            score               = score,
            isRunning           = !gameOver,
            gameOver            = gameOver,
            nextDifficultyScore = nextDiff,
            particles           = particles,
        )
    }

    fun resetGame() { _gameState.value = GameState() }

    fun saveHighScore(score: Int) {
        viewModelScope.launch { repository.saveHighScore(score) }
    }

    override fun onCleared() {
        audioController.release()
        super.onCleared()
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * How many spikes to spawn for this bounce.
     * Starts at 3, increases slowly with bounces/score up to MAX_SPIKES.
     */
    private fun calculateSpikeCount(bounceCount: Int, score: Int): Int {
        val base = 3 + (bounceCount / 3).coerceAtMost(4)
        return base.coerceAtMost(MAX_SPIKES)
    }

    /**
     * Generates [count] RELATIVE spike angles, avoiding [avoidRelAngle]
     * and maintaining minimum spacing between spikes.
     */
    private fun generateSpikes(count: Int, avoidRelAngle: Float): List<Float> {
        val spikes   = mutableListOf<Float>()
        var attempts = 0
        while (spikes.size < count && attempts < 500) {
            attempts++
            val c = Random.nextFloat() * 360f
            if (angleDiff(c, avoidRelAngle) < SAFE_ZONE_DEG) continue
            if (spikes.any { angleDiff(c, it) < MIN_SPIKE_GAP_DEG }) continue
            spikes.add(c)
        }
        return spikes
    }

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
