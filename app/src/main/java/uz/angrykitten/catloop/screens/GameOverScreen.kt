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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import uz.angrykitten.catloop.game.GameViewModel
import uz.angrykitten.catloop.ui.theme.CatLoopColors

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
        cardScale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 260f))
    }

    // Bounce animation for "NEW BEST" badge
    val bounce by rememberInfiniteTransition(label = "bounce").animateFloat(
        initialValue  = 0.94f,
        targetValue   = 1.06f,
        animationSpec = infiniteRepeatable(
            animation  = tween(500),
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
                        CatLoopColors.DarkRedCircle.copy(alpha = 0.08f),
                        CatLoopColors.Background,
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

            // ── Crash emoji + GAME OVER ────────────────────────────────────────
            Text(
                text     = "🐱💥",
                fontSize = 52.sp,
            )

            Spacer(Modifier.height(12.dp))

            // GAME OVER with layered shadow effect
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text       = "GAME OVER",
                    color      = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Black,
                    fontSize   = 42.sp,
                    modifier   = Modifier.offset(3.dp, 3.dp),
                )
                Text(
                    text       = "GAME OVER",
                    color      = CatLoopColors.DarkRedCircle,
                    fontWeight = FontWeight.Black,
                    fontSize   = 42.sp,
                )
            }

            Spacer(Modifier.height(36.dp))

            // ── Score card ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                CatLoopColors.DarkRedCircle.copy(alpha = 0.09f),
                                CatLoopColors.DarkRedCircle.copy(alpha = 0.04f),
                            )
                        )
                    )
                    .border(
                        width  = 1.dp,
                        color  = CatLoopColors.DarkRedCircle.copy(alpha = 0.2f),
                        shape  = RoundedCornerShape(24.dp),
                    )
                    .padding(vertical = 28.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text          = "YOUR SCORE",
                        color         = CatLoopColors.PrimaryOrange,
                        fontSize      = 12.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 3.sp,
                    )
                    Spacer(Modifier.height(6.dp))

                    // Big score number with shadow
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text       = score.toString(),
                            color      = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Black,
                            fontSize   = 80.sp,
                            modifier   = Modifier.offset(4.dp, 4.dp),
                        )
                        Text(
                            text       = score.toString(),
                            color      = CatLoopColors.TextBlack,
                            fontWeight = FontWeight.Black,
                            fontSize   = 80.sp,
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(2.dp)
                            .background(
                                CatLoopColors.DarkRedCircle.copy(alpha = 0.20f),
                                RoundedCornerShape(1.dp),
                            )
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text       = "BEST  ${maxOf(highScore, score)}",
                        color      = CatLoopColors.TextBlack.copy(alpha = 0.55f),
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                    )

                    if (isNewBest) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text       = "🎉  NEW BEST!",
                            color      = CatLoopColors.YellowOrb,
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Black,
                            modifier   = Modifier.scale(bounce),
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // ── PLAY AGAIN button ─────────────────────────────────────────────
            Button(
                onClick = {
                    viewModel.resetGame()
                    navController.navigate("game") {
                        popUpTo("menu")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CatLoopColors.DarkRedCircle,
                    contentColor   = Color.White,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            ) {
                Text(
                    "▶  PLAY AGAIN",
                    fontSize      = 18.sp,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 2.sp,
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── MAIN MENU outlined button ─────────────────────────────────────
            OutlinedButton(
                onClick = {
                    navController.navigate("menu") {
                        popUpTo("menu") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape  = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    color = CatLoopColors.PrimaryOrange,
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
