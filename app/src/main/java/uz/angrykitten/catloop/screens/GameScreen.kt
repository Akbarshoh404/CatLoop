package uz.angrykitten.catloop.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.navigation.NavController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import uz.angrykitten.catloop.game.GameViewModel
import uz.angrykitten.catloop.game.drawCat
import uz.angrykitten.catloop.game.drawCenterDivider
import uz.angrykitten.catloop.game.drawGameBackground
import uz.angrykitten.catloop.game.drawParticleTrail
import uz.angrykitten.catloop.game.drawRing
import uz.angrykitten.catloop.game.drawRotationHints
import uz.angrykitten.catloop.game.drawSafeRingGlow
import uz.angrykitten.catloop.game.drawScoreHUD
import uz.angrykitten.catloop.game.drawSpike
import uz.angrykitten.catloop.ui.theme.CatLoopColors
import androidx.compose.foundation.Canvas as ComposeCanvas

@Composable
fun GameScreen(navController: NavController, viewModel: GameViewModel) {

    val gameState    by viewModel.gameState.collectAsState()
    val textMeasurer = rememberTextMeasurer()

    // ── Back button → menu ─────────────────────────────────────────────────
    BackHandler {
        viewModel.onPressUp()
        navController.navigate("menu") {
            popUpTo("game") { inclusive = true }
        }
    }

    // ── Auto-start when this screen enters composition ─────────────────────
    LaunchedEffect(Unit) {
        viewModel.startGame()
    }

    // ── Navigate to Game Over as soon as the game ends ─────────────────────
    LaunchedEffect(gameState.gameOver) {
        if (gameState.gameOver) {
            navController.navigate("gameover/${gameState.score}") {
                popUpTo("game") { inclusive = true }
            }
        }
    }

    // ── 60-fps game loop ───────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        var lastFrameNanos = 0L
        while (isActive) {
            withFrameNanos { frameNanos ->
                val deltaMs = if (lastFrameNanos == 0L) 16f
                              else ((frameNanos - lastFrameNanos) / 1_000_000f)
                lastFrameNanos = frameNanos
                viewModel.update(deltaMs.coerceIn(4f, 32f))
            }
        }
    }

    // ── Full-screen touch: left = CCW, right = CW ──────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CatLoopColors.Background)
            .pointerInput(Unit) {
                coroutineScope {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: continue

                            when (event.type) {
                                PointerEventType.Press -> {
                                    val isRight = change.position.x > size.width / 2f
                                    viewModel.onPressDown(isRight)
                                    change.consume()
                                }
                                PointerEventType.Release -> {
                                    viewModel.onPressUp()
                                    change.consume()
                                }
                                else -> {}
                            }
                        }
                    }
                }
            },
    ) {

        // ── Game canvas ────────────────────────────────────────────────────
        ComposeCanvas(modifier = Modifier.fillMaxSize()) {

            val canvasSize  = size
            val centre      = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            val minDim      = minOf(canvasSize.width, canvasSize.height)

            // Pixel measures
            val ringRadius  = minDim * 0.40f
            val strokeWidth = 16f * density
            val spikeBase   = 24f * density
            val spikeHeight = 32f * density
            val catSize     = 22f * density

            val st = gameState

            // 1. Background
            drawGameBackground(canvasSize)

            // 2. Center divider line (left=CCW, right=CW indicator)
            drawCenterDivider(canvasSize)

            // 3. Score HUD (subtle watermark in ring center)
            drawScoreHUD(st.score, centre, textMeasurer)

            // 4. Safe ring glow on first pass (before any bounce)
            if (st.bounceCount == 0) {
                drawSafeRingGlow(centre, ringRadius, strokeWidth)
            }

            // 5. Ring
            drawRing(centre, ringRadius, strokeWidth)

            // 6. Rotation hint arrows (inside ring)
            drawRotationHints(centre, ringRadius, st.ringRotDir, density)

            // 7. Particle trail (behind the cat)
            drawParticleTrail(
                center     = centre,
                ringRadius = ringRadius,
                particles  = st.particles,
                catRadius  = catSize,
            )

            // 8. Spikes with pop-in scale (absolute angles = rel + ringAngle)
            st.spikes.forEachIndexed { i, relAngle ->
                val absAngle = (relAngle + st.ringAngle) % 360f
                val pop      = st.spikeScales.getOrElse(i) { 1f }
                drawSpike(
                    center             = centre,
                    ringRadius         = ringRadius,
                    absoluteSpikeAngle = absAngle,
                    spikeSize          = spikeBase,
                    spikeHeight        = spikeHeight,
                    popScale           = pop,
                )
            }

            // 9. Cat (top layer)
            drawCat(
                center     = centre,
                ringRadius = ringRadius,
                catNormX   = st.catX,
                catNormY   = st.catY,
                velX       = st.velX,
                velY       = st.velY,
                catSize    = catSize,
                catScale   = st.catScale,
            )
        }
    }
}
