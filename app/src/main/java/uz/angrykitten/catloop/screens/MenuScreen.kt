package uz.angrykitten.catloop.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import uz.angrykitten.catloop.R
import uz.angrykitten.catloop.game.GameViewModel
import uz.angrykitten.catloop.ui.components.drawPawPrints
import uz.angrykitten.catloop.ui.theme.CatLoopColors
import androidx.compose.foundation.Canvas as ComposeCanvas

@Composable
fun MenuScreen(navController: NavController, viewModel: GameViewModel) {

    val highScore by viewModel.highScore.collectAsState(initial = 0)

    // Entry animation state
    val logoAlpha  = remember { Animatable(0f) }
    val logoScale  = remember { Animatable(0.8f) }
    val titleAlpha = remember { Animatable(0f) }
    val btnAlpha   = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(400))
        logoScale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 200f))
        titleAlpha.animateTo(1f, tween(350))
        btnAlpha.animateTo(1f, tween(400))
    }

    // Subtle pulse on the ring decoration
    val pulse by rememberInfiniteTransition(label = "menuPulse").animateFloat(
        initialValue = 0.97f,
        targetValue  = 1.03f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
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
            )
    ) {
        // ── Paw-print watermark background ───────────────────────────────────
        ComposeCanvas(modifier = Modifier.fillMaxSize()) {
            drawPawPrints(size, alpha = 0.05f)
        }

        // ── Decorative ring behind logo ───────────────────────────────────────
        ComposeCanvas(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.TopCenter)
                .offset(y = 90.dp)
                .scale(pulse)
                .alpha(0.5f)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r  = minOf(size.width, size.height) / 2f * 0.88f
            drawCircle(
                color  = CatLoopColors.DarkRedCircle.copy(alpha = 0.12f),
                radius = r,
                center = Offset(cx, cy),
                style  = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f * density),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            // ── Cat logo image ────────────────────────────────────────────────
            Image(
                painter            = painterResource(id = R.drawable.cat),
                contentDescription = "CatLoop logo",
                contentScale       = ContentScale.Fit,
                modifier           = Modifier
                    .size(180.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )

            Spacer(Modifier.height(20.dp))

            // ── App title ─────────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(titleAlpha.value),
            ) {
                Text(
                    text          = "CatLoop",
                    color         = CatLoopColors.TextBlack,
                    fontWeight    = FontWeight.Black,
                    fontSize      = 48.sp,
                    letterSpacing = (-1).sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text          = "by AngryKitten",
                    color         = CatLoopColors.PrimaryOrange,
                    fontSize      = 13.sp,
                    fontWeight    = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                )
            }

            Spacer(Modifier.height(40.dp))

            // ── Buttons column ────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(btnAlpha.value),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // PLAY button
                MenuPrimaryButton(
                    label = "PLAY",
                    onClick = {
                        viewModel.resetGame()
                        navController.navigate("game")
                    },
                )

                // SETTINGS button
                MenuOutlineButton(
                    label = "SETTINGS",
                    onClick = { navController.navigate("settings") },
                )
            }

            Spacer(Modifier.height(40.dp))

            // ── High Score display ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CatLoopColors.DarkRedCircle.copy(alpha = 0.07f))
                    .border(
                        1.dp,
                        CatLoopColors.DarkRedCircle.copy(alpha = 0.15f),
                        RoundedCornerShape(20.dp),
                    )
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text          = "BEST SCORE",
                        color         = CatLoopColors.PrimaryOrange,
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 3.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = highScore.toString(),
                        color      = CatLoopColors.TextBlack,
                        fontWeight = FontWeight.Black,
                        fontSize   = 44.sp,
                        letterSpacing = (-1).sp,
                    )
                }
            }
        }

        // ── Settings icon (top-right) ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 20.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(CatLoopColors.DarkRedCircle.copy(alpha = 0.1f))
                .border(1.dp, CatLoopColors.DarkRedCircle.copy(alpha = 0.2f), CircleShape)
                .clickable { navController.navigate("settings") },
            contentAlignment = Alignment.Center,
        ) {
            // Settings gear icon drawn with Canvas
            ComposeCanvas(modifier = Modifier.size(22.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r  = size.width * 0.28f
                val outerR = size.width * 0.48f
                // Outer circle
                drawCircle(
                    color  = CatLoopColors.DarkRedCircle,
                    radius = outerR,
                    center = Offset(cx, cy),
                    style  = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.2f * density),
                )
                // Inner circle
                drawCircle(
                    color  = CatLoopColors.DarkRedCircle,
                    radius = r,
                    center = Offset(cx, cy),
                )
                // Gear teeth (8 rectangles around the circle)
                for (i in 0 until 8) {
                    val angle = Math.toRadians((i * 45.0)).toFloat()
                    val tx = cx + kotlin.math.cos(angle) * outerR
                    val ty = cy + kotlin.math.sin(angle) * outerR
                    drawCircle(
                        color  = CatLoopColors.DarkRedCircle,
                        radius = 2f * density,
                        center = Offset(tx, ty),
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuPrimaryButton(label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.96f else 1f

    Button(
        onClick           = onClick,
        interactionSource = interactionSource,
        modifier          = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .scale(scale),
        shape  = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CatLoopColors.DarkRedCircle,
            contentColor   = Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp,
        ),
    ) {
        Text(
            text          = label,
            fontSize      = 20.sp,
            fontWeight    = FontWeight.Black,
            letterSpacing = 4.sp,
        )
    }
}

@Composable
private fun MenuOutlineButton(label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.96f else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.Transparent)
            .border(
                width = 1.5.dp,
                color = CatLoopColors.DarkRedCircle.copy(alpha = 0.4f),
                shape = RoundedCornerShape(18.dp),
            )
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text          = label,
            fontSize      = 16.sp,
            fontWeight    = FontWeight.Bold,
            color         = CatLoopColors.DarkRedCircle,
            letterSpacing = 3.sp,
        )
    }
}
