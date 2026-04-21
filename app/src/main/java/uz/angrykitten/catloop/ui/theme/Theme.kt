package uz.angrykitten.catloop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CatLoopColorScheme = lightColorScheme(
    primary        = CatLoopColors.DarkRedCircle,
    onPrimary      = CatLoopColors.White,
    secondary      = CatLoopColors.PrimaryOrange,
    onSecondary    = CatLoopColors.TextBlack,
    tertiary       = CatLoopColors.YellowOrb,
    background     = CatLoopColors.Background,
    onBackground   = CatLoopColors.TextBlack,
    surface        = CatLoopColors.Background,
    onSurface      = CatLoopColors.TextBlack,
)

@Composable
fun CatLoopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CatLoopColorScheme,
        typography  = Typography,
        content     = content
    )
}