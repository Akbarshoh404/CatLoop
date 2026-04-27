package uz.angrykitten.catloop.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import uz.angrykitten.catloop.game.GameViewModel
import uz.angrykitten.catloop.ui.components.drawPawPrints
import uz.angrykitten.catloop.ui.theme.CatLoopColors
import androidx.compose.foundation.Canvas as ComposeCanvas

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: GameViewModel,
) {
    val highScore by viewModel.highScore.collectAsState(initial = 0)
    val musicEnabled by viewModel.isMusicEnabled.collectAsState()

    var vibrationEnabled by remember { mutableStateOf(true) }

    // Entry animation
    val contentAlpha = remember { Animatable(0f) }
    val contentScale = remember { Animatable(0.94f) }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = 300f))
        contentScale.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 280f))
    }

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
    ) {
        // Paw watermark
        ComposeCanvas(modifier = Modifier.fillMaxSize()) {
            drawPawPrints(size, alpha = 0.04f)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .scale(contentScale.value)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(52.dp))

            // ── Header row ───────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Back button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CatLoopColors.DarkRedCircle.copy(alpha = 0.1f))
                        .border(1.dp, CatLoopColors.DarkRedCircle.copy(alpha = 0.2f), CircleShape)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center,
                ) {
                    // Left arrow icon (no emoji, drawn)
                    ComposeCanvas(modifier = Modifier.size(20.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val arrowPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(cx + size.width * 0.3f, cy - size.height * 0.3f)
                            lineTo(cx - size.width * 0.2f, cy)
                            lineTo(cx + size.width * 0.3f, cy + size.height * 0.3f)
                        }
                        drawPath(
                            arrowPath,
                            color = CatLoopColors.DarkRedCircle,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 2.5f * density,
                                join  = androidx.compose.ui.graphics.StrokeJoin.Round,
                                cap   = androidx.compose.ui.graphics.StrokeCap.Round,
                            ),
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Text(
                    text          = "SETTINGS",
                    color         = CatLoopColors.TextBlack,
                    fontWeight    = FontWeight.Black,
                    fontSize      = 26.sp,
                    letterSpacing = 3.sp,
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Settings sections ────────────────────────────────────────────
            SettingsSection(title = "AUDIO") {
                SettingsToggleRow(
                    label       = "Music",
                    description = "Background music",
                    checked     = musicEnabled,
                    onToggle    = viewModel::setMusicEnabled,
                )
            }

            Spacer(Modifier.height(16.dp))

            SettingsSection(title = "GAMEPLAY") {
                SettingsToggleRow(
                    label       = "Vibration",
                    description = "Haptic feedback on bounce",
                    checked     = vibrationEnabled,
                    onToggle    = { vibrationEnabled = it },
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Stats section ────────────────────────────────────────────────
            SettingsSection(title = "STATS") {
                SettingsInfoRow(label = "Best Score", value = highScore.toString())
                SettingsDivider()
                SettingsInfoRow(label = "Version", value = "1.0.0")
            }

            Spacer(Modifier.height(24.dp))

            // ── Reset high score ─────────────────────────────────────────────
            var confirmReset by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (confirmReset) CatLoopColors.DarkRedCircle.copy(alpha = 0.12f)
                        else Color.Transparent
                    )
                    .border(
                        1.5.dp,
                        CatLoopColors.DarkRedCircle.copy(alpha = if (confirmReset) 0.5f else 0.25f),
                        RoundedCornerShape(16.dp),
                    )
                    .clickable {
                        if (confirmReset) {
                            viewModel.saveHighScore(0)
                            confirmReset = false
                        } else {
                            confirmReset = true
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text          = if (confirmReset) "TAP AGAIN TO CONFIRM" else "RESET HIGH SCORE",
                    color         = CatLoopColors.DarkRedCircle,
                    fontSize      = 14.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Credits ──────────────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text          = "CatLoop",
                    color         = CatLoopColors.TextBlack.copy(alpha = 0.4f),
                    fontSize      = 13.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                Text(
                    text          = "Made with love by AngryKitten",
                    color         = CatLoopColors.TextBlack.copy(alpha = 0.3f),
                    fontSize      = 11.sp,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text          = title,
            color         = CatLoopColors.PrimaryOrange,
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 3.sp,
            modifier      = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CatLoopColors.DarkRedCircle.copy(alpha = 0.06f))
                .border(
                    1.dp,
                    CatLoopColors.DarkRedCircle.copy(alpha = 0.12f),
                    RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment    = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = label,
                color      = CatLoopColors.TextBlack,
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text     = description,
                color    = CatLoopColors.TextBlack.copy(alpha = 0.45f),
                fontSize = 12.sp,
            )
        }
        Switch(
            checked         = checked,
            onCheckedChange = onToggle,
            colors          = SwitchDefaults.colors(
                checkedThumbColor   = Color.White,
                checkedTrackColor   = CatLoopColors.DarkRedCircle,
                uncheckedThumbColor = CatLoopColors.TextBlack.copy(alpha = 0.3f),
                uncheckedTrackColor = CatLoopColors.TextBlack.copy(alpha = 0.1f),
            ),
        )
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment    = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text       = label,
            color      = CatLoopColors.TextBlack,
            fontSize   = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text       = value,
            color      = CatLoopColors.DarkRedCircle,
            fontSize   = 15.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(CatLoopColors.DarkRedCircle.copy(alpha = 0.1f))
    )
}
