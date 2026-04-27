package uz.angrykitten.catloop.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import uz.angrykitten.catloop.game.GameViewModel
import uz.angrykitten.catloop.ui.theme.CatLoopColors
import androidx.compose.foundation.Canvas as ComposeCanvas

@Composable
fun GameOverScreen(
    navController: NavController,
    score: Int,
    viewModel: GameViewModel
) {
    val highScore by viewModel.highScore.collectAsState(initial = 0)
    val isNewBest = score > highScore

    // Save high score once on entry
    LaunchedEffect(Unit) {
        viewModel.saveHighScore(score)
    }

    // Entry animation
    val cardScale = remember { Animatable(0.85f) }
    LaunchedEffect(Unit) {
        cardScale.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = 260f))
    }

    // Bounce animation for "NEW BEST" badge
    val bounce by rememberInfiniteTransition(label = "bounce").animateFloat(
        initialValue  = 0.94f,
        targetValue   = 1.06f,
        animationSpec = infiniteRepeatable(
            animation  = tween(480),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bounceScale",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CatLoopColors.Background,
                        Color(0xFFEDE4D4),
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .scale(cardScale.value),
        ) {

            // ── Cat crash indicator (drawn icon, no emoji) ─────────────────
            CatCrashIcon()

            Spacer(Modifier.height(16.dp))

            // GAME OVER
            Text(
                text          = "GAME OVER",
                color         = CatLoopColors.DarkRedCircle,
                fontWeight    = FontWeight.Black,
                fontSize      = 40.sp,
                letterSpacing = 2.sp,
            )

            Spacer(Modifier.height(32.dp))

            // ── Score card ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                CatLoopColors.DarkRedCircle.copy(alpha = 0.08f),
                                CatLoopColors.DarkRedCircle.copy(alpha = 0.03f),
                            )
                        )
                    )
                    .border(
                        width  = 1.dp,
                        color  = CatLoopColors.DarkRedCircle.copy(alpha = 0.18f),
                        shape  = RoundedCornerShape(24.dp),
                    )
                    .padding(vertical = 28.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text          = "YOUR SCORE",
                        color         = CatLoopColors.PrimaryOrange,
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 3.sp,
                    )
                    Spacer(Modifier.height(6.dp))

                    Text(
                        text          = score.toString(),
                        color         = CatLoopColors.TextBlack,
                        fontWeight    = FontWeight.Black,
                        fontSize      = 80.sp,
                        letterSpacing = (-2).sp,
                    )

                    Spacer(Modifier.height(8.dp))

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(1.5.dp)
                            .background(
                                CatLoopColors.DarkRedCircle.copy(alpha = 0.18f),
                                RoundedCornerShape(1.dp),
                            )
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text          = "BEST  ${maxOf(highScore, score)}",
                        color         = CatLoopColors.TextBlack.copy(alpha = 0.5f),
                        fontSize      = 18.sp,
                        fontWeight    = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                    )

                    if (isNewBest) {
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .scale(bounce)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CatLoopColors.YellowOrb.copy(alpha = 0.18f))
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text          = "NEW BEST",
                                color         = Color(0xFFB8860B),
                                fontSize      = 14.sp,
                                fontWeight    = FontWeight.Black,
                                letterSpacing = 2.sp,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            // ── PLAY AGAIN button ─────────────────────────────────────────
            Button(
                onClick = {
                    viewModel.resetGame()
                    navController.navigate("game") {
                        popUpTo("menu")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CatLoopColors.DarkRedCircle,
                    contentColor   = Color.White,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            ) {
                Text(
                    "PLAY AGAIN",
                    fontSize      = 18.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 3.sp,
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── MAIN MENU button ──────────────────────────────────────────
            OutlinedButton(
                onClick = {
                    navController.navigate("menu") {
                        popUpTo("menu") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = CatLoopColors.PrimaryOrange.copy(alpha = 0.5f),
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CatLoopColors.PrimaryOrange,
                ),
            ) {
                Text(
                    "MAIN MENU",
                    fontSize      = 16.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }
        }
    }
}

/** Draws a minimal cat + X icon without emojis. */
@Composable
private fun CatCrashIcon() {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(50))
            .background(CatLoopColors.DarkRedCircle.copy(alpha = 0.1f))
            .border(1.5.dp, CatLoopColors.DarkRedCircle.copy(alpha = 0.25f), RoundedCornerShape(50)),
        contentAlignment = Alignment.Center,
    ) {
        ComposeCanvas(modifier = Modifier.size(40.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r  = size.width * 0.38f

            // Cat head circle
            drawCircle(
                color  = CatLoopColors.PrimaryOrange,
                radius = r,
                center = androidx.compose.ui.geometry.Offset(cx, cy),
            )
            drawCircle(
                color  = CatLoopColors.DarkRedCircle,
                radius = r,
                center = androidx.compose.ui.geometry.Offset(cx, cy),
                style  = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f * density),
            )

            // X eyes
            val eyeOffset = r * 0.38f
            val eyeSize   = r * 0.18f
            val xColor    = CatLoopColors.DarkRedCircle

            // Left X
            drawLine(xColor, androidx.compose.ui.geometry.Offset(cx - eyeOffset - eyeSize, cy - eyeSize),
                androidx.compose.ui.geometry.Offset(cx - eyeOffset + eyeSize, cy + eyeSize), 2f * density)
            drawLine(xColor, androidx.compose.ui.geometry.Offset(cx - eyeOffset + eyeSize, cy - eyeSize),
                androidx.compose.ui.geometry.Offset(cx - eyeOffset - eyeSize, cy + eyeSize), 2f * density)

            // Right X
            drawLine(xColor, androidx.compose.ui.geometry.Offset(cx + eyeOffset - eyeSize, cy - eyeSize),
                androidx.compose.ui.geometry.Offset(cx + eyeOffset + eyeSize, cy + eyeSize), 2f * density)
            drawLine(xColor, androidx.compose.ui.geometry.Offset(cx + eyeOffset + eyeSize, cy - eyeSize),
                androidx.compose.ui.geometry.Offset(cx + eyeOffset - eyeSize, cy + eyeSize), 2f * density)

            // Left ear
            val earPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx - r * 0.35f, cy - r * 0.72f)
                lineTo(cx - r * 0.72f, cy - r * 1.22f)
                lineTo(cx - r * 0.02f, cy - r * 0.88f)
                close()
            }
            drawPath(earPath, color = CatLoopColors.PrimaryOrange)
            drawPath(earPath, color = CatLoopColors.DarkRedCircle,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f * density, join = androidx.compose.ui.graphics.StrokeJoin.Round))

            // Right ear
            val earPath2 = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx + r * 0.02f, cy - r * 0.88f)
                lineTo(cx + r * 0.72f, cy - r * 1.22f)
                lineTo(cx + r * 0.35f, cy - r * 0.72f)
                close()
            }
            drawPath(earPath2, color = CatLoopColors.PrimaryOrange)
            drawPath(earPath2, color = CatLoopColors.DarkRedCircle,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f * density, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        }
    }
}
