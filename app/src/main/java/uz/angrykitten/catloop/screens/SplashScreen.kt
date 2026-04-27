package uz.angrykitten.catloop.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import kotlinx.coroutines.delay
import uz.angrykitten.catloop.R
import uz.angrykitten.catloop.ui.theme.CatLoopColors
import androidx.compose.foundation.Canvas as ComposeCanvas

@Composable
fun SplashScreen(navController: NavController) {

    // Navigate to menu after 2.8s
    LaunchedEffect(Unit) {
        delay(2800)
        navController.navigate("menu") {
            popUpTo("splash") { inclusive = true }
        }
    }

    // ── Animations ────────────────────────────────────────────────────────
    val logoScale  = remember { Animatable(0f) }
    val logoAlpha  = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val tagAlpha   = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo pops in with spring overshoot
        logoAlpha.animateTo(1f, tween(300))
        logoScale.animateTo(1f, tween(520, easing = EaseOutBack))
        delay(80)
        // Title fades in
        titleAlpha.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
        delay(80)
        tagAlpha.animateTo(1f, tween(320))
    }

    // Subtle pulsing ring behind cat
    val pulse by rememberInfiniteTransition(label = "splashPulse").animateFloat(
        initialValue  = 0.94f,
        targetValue   = 1.06f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors  = listOf(Color(0xFFFAF4EA), Color(0xFFEFE4D0)),
                    radius  = 1200f,
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        // ── Decorative rings ───────────────────────────────────────────────
        ComposeCanvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val base = minOf(size.width, size.height) * 0.38f
            drawCircle(
                color  = CatLoopColors.DarkRedCircle.copy(alpha = 0.07f),
                radius = base * pulse,
                center = Offset(cx, cy),
                style  = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f * density),
            )
            drawCircle(
                color  = CatLoopColors.DarkRedCircle.copy(alpha = 0.04f),
                radius = base * 1.28f * pulse,
                center = Offset(cx, cy),
                style  = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f * density),
            )
            drawCircle(
                color  = CatLoopColors.PrimaryOrange.copy(alpha = 0.04f),
                radius = base * 1.6f * pulse,
                center = Offset(cx, cy),
                style  = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f * density),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Cat image from drawable/cat.png ────────────────────────────
            Image(
                painter            = painterResource(id = R.drawable.cat),
                contentDescription = "CatLoop cat",
                contentScale       = ContentScale.Fit,
                modifier           = Modifier
                    .size(190.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value),
            )

            Spacer(Modifier.height(28.dp))

            // ── Title ─────────────────────────────────────────────────────
            Text(
                text          = "CatLoop",
                color         = CatLoopColors.TextBlack,
                fontWeight    = FontWeight.Black,
                fontSize      = 52.sp,
                letterSpacing = (-1.5).sp,
                modifier      = Modifier.alpha(titleAlpha.value),
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text          = "by AngryKitten",
                color         = CatLoopColors.PrimaryOrange,
                fontWeight    = FontWeight.Medium,
                fontSize      = 13.sp,
                letterSpacing = 2.5.sp,
                modifier      = Modifier.alpha(tagAlpha.value),
            )

            Spacer(Modifier.height(52.dp))

            // ── Animated loading dots ──────────────────────────────────────
            LoadingDots(modifier = Modifier.alpha(tagAlpha.value))
        }
    }
}

@Composable
private fun LoadingDots(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "dots")

    val a1 by infinite.animateFloat(
        initialValue  = 0.25f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse,
        ), label = "d1",
    )
    val a2 by infinite.animateFloat(
        initialValue  = 0.25f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(550, delayMillis = 140, easing = FastOutSlowInEasing), RepeatMode.Reverse,
        ), label = "d2",
    )
    val a3 by infinite.animateFloat(
        initialValue  = 0.25f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(550, delayMillis = 280, easing = FastOutSlowInEasing), RepeatMode.Reverse,
        ), label = "d3",
    )

    ComposeCanvas(modifier = modifier.size(width = 44.dp, height = 8.dp)) {
        val r   = 3.6f * density
        val gap = 13f * density
        val cx  = size.width / 2f
        val cy  = size.height / 2f
        drawCircle(CatLoopColors.DarkRedCircle.copy(alpha = a1), r, Offset(cx - gap, cy))
        drawCircle(CatLoopColors.DarkRedCircle.copy(alpha = a2), r, Offset(cx, cy))
        drawCircle(CatLoopColors.DarkRedCircle.copy(alpha = a3), r, Offset(cx + gap, cy))
    }
}
