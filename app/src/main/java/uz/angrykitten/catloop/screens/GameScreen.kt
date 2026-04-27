package uz.angrykitten.catloop.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import uz.angrykitten.catloop.R
import uz.angrykitten.catloop.game.GameViewModel
import uz.angrykitten.catloop.game.drawCat
import uz.angrykitten.catloop.game.drawCenterDivider
import uz.angrykitten.catloop.game.drawGameBackground
import uz.angrykitten.catloop.game.drawParticleTrail
import uz.angrykitten.catloop.game.drawRing
import uz.angrykitten.catloop.game.drawRotationHints
import uz.angrykitten.catloop.game.drawScoreHUD
import uz.angrykitten.catloop.game.drawSpike
import uz.angrykitten.catloop.ui.theme.CatLoopColors
import androidx.compose.foundation.Canvas as ComposeCanvas

@Composable
fun GameScreen(navController: NavController, viewModel: GameViewModel) {

    val gameState    by viewModel.gameState.collectAsState()
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()

    // Load the logo.png bitmap to use as the cat sprite
    val catBitmap: ImageBitmap = ImageBitmap.imageResource(R.drawable.cat)

    // ── Back button → pause menu (not direct exit) ─────────────────────────
    BackHandler {
        if (gameState.isPaused) {
            viewModel.onPressUp()
            navController.navigate("menu") {
                popUpTo("game") { inclusive = true }
            }
        } else {
            viewModel.pauseGame()
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

                            // Don't respond to game input while paused
                            if (gameState.isPaused) continue

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

            // 4. Ring
            drawRing(centre, ringRadius, strokeWidth)

            // 5. Rotation hint arrows (inside ring)
            drawRotationHints(centre, ringRadius, st.ringRotDir, density)

            // 6. Particle trail (behind the cat)
            drawParticleTrail(
                center     = centre,
                ringRadius = ringRadius,
                particles  = st.particles,
                catRadius  = catSize,
            )

            // 7. Spikes with pop-in scale (absolute angles = rel + ringAngle)
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

            // 8. Cat (top layer) — use logo.png bitmap
            drawCat(
                center     = centre,
                ringRadius = ringRadius,
                catNormX   = st.catX,
                catNormY   = st.catY,
                velX       = st.velX,
                velY       = st.velY,
                catSize    = catSize,
                catScale   = st.catScale,
                catBitmap  = catBitmap,
            )

            // 9. Score HUD above the ring
            drawScoreHUD(
                score       = st.score,
                ringCenter  = centre,
                ringRadius  = ringRadius,
                strokeWidth = strokeWidth,
                textMeasurer = textMeasurer,
            )
        }

        // ── Pause button (top-right) ────────────────────────────────────────
        if (!gameState.isPaused) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CatLoopColors.DarkRedCircle.copy(alpha = 0.12f))
                        .border(1.dp, CatLoopColors.DarkRedCircle.copy(alpha = 0.25f), CircleShape)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val evt = awaitPointerEvent()
                                    if (evt.type == PointerEventType.Release) {
                                        viewModel.pauseGame()
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    // Pause icon drawn as two vertical bars
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(
                            Modifier
                                .width(4.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(CatLoopColors.DarkRedCircle)
                        )
                        Box(
                            Modifier
                                .width(4.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(CatLoopColors.DarkRedCircle)
                        )
                    }
                }
            }
        }

        // ── Pause menu overlay ─────────────────────────────────────────────
        AnimatedVisibility(
            visible = gameState.isPaused,
            enter   = fadeIn(tween(200)) + scaleIn(tween(220), initialScale = 0.92f),
            exit    = fadeOut(tween(160)) + scaleOut(tween(160), targetScale = 0.94f),
        ) {
            PauseMenuOverlay(
                score       = gameState.score,
                onResume    = { viewModel.resumeGame() },
                onRestart   = {
                    viewModel.resetGame()
                    navController.navigate("game") {
                        popUpTo("game") { inclusive = true }
                    }
                },
                onMainMenu  = {
                    viewModel.resetGame()
                    navController.navigate("menu") {
                        popUpTo("game") { inclusive = true }
                    }
                },
            )
        }
    }
}

@Composable
private fun PauseMenuOverlay(
    score: Int,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit,
) {
    val cardScale = remember { Animatable(0.88f) }
    LaunchedEffect(Unit) {
        cardScale.animateTo(1f, spring(dampingRatio = 0.65f, stiffness = 280f))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp)
                .scale(cardScale.value)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CatLoopColors.Background,
                            CatLoopColors.Background.copy(alpha = 0.97f),
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = CatLoopColors.DarkRedCircle.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(28.dp),
                )
                .padding(vertical = 36.dp, horizontal = 28.dp),
        ) {
            // Pause icon indicator
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    Modifier
                        .width(6.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(CatLoopColors.DarkRedCircle)
                )
                Box(
                    Modifier
                        .width(6.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(CatLoopColors.DarkRedCircle)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text       = "PAUSED",
                fontSize   = 32.sp,
                fontWeight = FontWeight.Black,
                color      = CatLoopColors.TextBlack,
                letterSpacing = 4.sp,
            )

            Spacer(Modifier.height(8.dp))

            // Score display
            Text(
                text          = "SCORE",
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = CatLoopColors.PrimaryOrange,
                letterSpacing = 3.sp,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text       = score.toString(),
                fontSize   = 52.sp,
                fontWeight = FontWeight.Black,
                color      = CatLoopColors.TextBlack,
            )

            Spacer(Modifier.height(32.dp))

            // ── RESUME button ──────────────────────────────────────────────
            Button(
                onClick  = onResume,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CatLoopColors.DarkRedCircle,
                    contentColor   = Color.White,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            ) {
                Text(
                    "RESUME",
                    fontSize      = 17.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 3.sp,
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── RESTART button ─────────────────────────────────────────────
            OutlinedButton(
                onClick  = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape  = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = CatLoopColors.DarkRedCircle.copy(alpha = 0.5f),
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CatLoopColors.DarkRedCircle,
                ),
            ) {
                Text(
                    "RESTART",
                    fontSize      = 15.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── MAIN MENU button ───────────────────────────────────────────
            OutlinedButton(
                onClick  = onMainMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape  = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = CatLoopColors.PrimaryOrange.copy(alpha = 0.55f),
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CatLoopColors.PrimaryOrange,
                ),
            ) {
                Text(
                    "MAIN MENU",
                    fontSize      = 15.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }
        }
    }
}
