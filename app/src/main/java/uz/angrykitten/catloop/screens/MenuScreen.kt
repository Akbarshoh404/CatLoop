package uz.angrykitten.catloop.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

    // Trigger fade-in animation on entry
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CatLoopColors.Background)
    ) {
        // ── Paw-print watermark background ───────────────────────────────────
        ComposeCanvas(modifier = Modifier.fillMaxSize()) {
            drawPawPrints(size, alpha = 0.06f)
        }

        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // ── Logo — uses the logo.png from the drawable ───────────────
                Image(
                    painter            = painterResource(id = R.drawable.logo),
                    contentDescription = "CatLoop logo",
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier.size(210.dp)
                )

                Spacer(Modifier.height(16.dp))

                // ── App title ─────────────────────────────────────────────────
                Box(contentAlignment = Alignment.Center) {
                    Text("CatLoop", color = Color.White, fontWeight = FontWeight.Black,
                        fontSize = 52.sp, modifier = Modifier.offset(3.dp, 3.dp))
                    Text("CatLoop", color = CatLoopColors.TextBlack, fontWeight = FontWeight.Black,
                        fontSize = 52.sp)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text          = "by AngryKitten",
                    color         = CatLoopColors.PrimaryOrange,
                    fontSize      = 15.sp,
                    fontWeight    = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(48.dp))

                // ── PLAY button ───────────────────────────────────────────────
                Button(
                    onClick = {
                        viewModel.resetGame()
                        navController.navigate("game")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CatLoopColors.DarkRedCircle,
                        contentColor   = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        text          = "PLAY",
                        fontSize      = 22.sp,
                        fontWeight    = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                }

                Spacer(Modifier.height(28.dp))

                // ── High Score ────────────────────────────────────────────────
                Text(
                    text          = "HIGH SCORE",
                    color         = CatLoopColors.PrimaryOrange,
                    fontSize      = 12.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
                Spacer(Modifier.height(4.dp))
                Box(contentAlignment = Alignment.Center) {
                    Text(highScore.toString(), color = Color.White,
                        fontWeight = FontWeight.Black, fontSize = 40.sp,
                        modifier = Modifier.offset(2.dp, 2.dp))
                    Text(highScore.toString(), color = CatLoopColors.TextBlack,
                        fontWeight = FontWeight.Black, fontSize = 40.sp)
                }
            }
        }
    }
}
