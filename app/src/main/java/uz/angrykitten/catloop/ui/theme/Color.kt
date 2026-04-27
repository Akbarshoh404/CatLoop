package uz.angrykitten.catloop.ui.theme

import androidx.compose.ui.graphics.Color

object CatLoopColors {
    // ── Primary palette ───────────────────────────────────────────────────────
    val PrimaryOrange = Color(0xFFE8881A)     // warmer, richer orange
    val RedAccent     = Color(0xFFD93025)     // vivid red for spikes
    val YellowOrb     = Color(0xFFF5C518)     // golden yellow
    val DarkRedCircle = Color(0xFF8B1A1A)     // deep burgundy-red for ring & accents

    // ── Surfaces ──────────────────────────────────────────────────────────────
    val Background    = Color(0xFFF7F0E6)     // warm cream
    val Surface       = Color(0xFFFDF8F2)     // slightly lighter surface
    val SurfaceCard   = Color(0xFFFAF3E8)     // card background

    // ── Text ──────────────────────────────────────────────────────────────────
    val TextBlack     = Color(0xFF1C1208)     // warm near-black
    val TextMuted     = Color(0xFF7A6652)     // muted warm brown

    // ── Misc ──────────────────────────────────────────────────────────────────
    val White         = Color(0xFFFFFFFF)
    val DarkBrown     = Color(0xFF3E1F00)
    val LightPink     = Color(0xFFFFB3C1)
}

// Legacy Material color aliases so Theme.kt keeps compiling
val Purple80       = Color(0xFFD0BCFF)
val PurpleGrey80   = Color(0xFFCCC2DC)
val Pink80         = Color(0xFFEFB8C8)
val Purple40       = Color(0xFF6650A4)
val PurpleGrey40   = Color(0xFF625B71)
val Pink40         = Color(0xFF7D5260)