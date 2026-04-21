package uz.angrykitten.catloop.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import uz.angrykitten.catloop.ui.components.drawCatLogo
import uz.angrykitten.catloop.ui.theme.CatLoopColors
import androidx.compose.foundation.Canvas as ComposeCanvas

@Composable
fun SplashScreen(navController: NavController) {

    LaunchedEffect(Unit) {
        delay(2500)
        navController.navigate("menu") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CatLoopColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // ── Logo ──────────────────────────────────────────────────────────
            ComposeCanvas(modifier = Modifier.size(220.dp)) {
                drawCatLogo(
                    center = Offset(size.width / 2f, size.height * 0.46f),
                    size   = size.width * 0.80f
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Title with white outline trick ────────────────────────────────
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text       = "CatLoop",
                    color      = CatLoopColors.White,
                    fontWeight = FontWeight.Black,
                    fontSize   = 54.sp,
                    modifier   = Modifier.offset(3.dp, 3.dp)
                )
                Text(
                    text       = "CatLoop",
                    color      = CatLoopColors.TextBlack,
                    fontWeight = FontWeight.Black,
                    fontSize   = 54.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text        = "by AngryKitten",
                color       = CatLoopColors.PrimaryOrange,
                fontWeight  = FontWeight.SemiBold,
                fontSize    = 16.sp,
                letterSpacing = 1.2.sp
            )
        }
    }
}
