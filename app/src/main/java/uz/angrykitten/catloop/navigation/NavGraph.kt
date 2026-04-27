package uz.angrykitten.catloop.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import uz.angrykitten.catloop.game.GameViewModel
import uz.angrykitten.catloop.screens.GameOverScreen
import uz.angrykitten.catloop.screens.GameScreen
import uz.angrykitten.catloop.screens.MenuScreen
import uz.angrykitten.catloop.screens.SettingsScreen
import uz.angrykitten.catloop.screens.SplashScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    // Single shared ViewModel scoped to the NavGraph composable lifetime
    val gameViewModel: GameViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    fun syncMusicForRoute(route: String?) {
        when {
            route == "game" -> gameViewModel.playGameMusic()
            route == "splash" || route == "menu" || route == "settings" || route?.startsWith("gameover") == true -> {
                gameViewModel.playMenuMusic()
            }
            else -> gameViewModel.stopMusic()
        }
    }

    LaunchedEffect(currentRoute) {
        syncMusicForRoute(currentRoute)
    }

    DisposableEffect(lifecycleOwner, currentRoute) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> syncMusicForRoute(currentRoute)
                Lifecycle.Event.ON_STOP -> gameViewModel.stopMusic()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    NavHost(
        navController    = navController,
        startDestination = "splash",
        enterTransition  = { fadeIn(tween(350)) },
        exitTransition   = { fadeOut(tween(250)) },
    ) {

        // ── Splash ────────────────────────────────────────────────────────
        composable("splash") {
            SplashScreen(navController = navController)
        }

        // ── Menu ──────────────────────────────────────────────────────────
        composable(
            route = "menu",
            enterTransition = { fadeIn(tween(450)) },
            exitTransition  = { fadeOut(tween(250)) },
        ) {
            MenuScreen(navController = navController, viewModel = gameViewModel)
        }

        // ── Settings ──────────────────────────────────────────────────────
        composable(
            route = "settings",
            enterTransition = {
                slideInHorizontally(tween(350)) { it } + fadeIn(tween(350))
            },
            exitTransition  = {
                slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300))
            },
        ) {
            SettingsScreen(navController = navController, viewModel = gameViewModel)
        }

        // ── Game ──────────────────────────────────────────────────────────
        composable(
            route = "game",
            enterTransition = {
                slideInHorizontally(tween(350)) { it } + fadeIn(tween(350))
            },
            exitTransition  = {
                slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300))
            },
        ) {
            GameScreen(navController = navController, viewModel = gameViewModel)
        }

        // ── Game Over ─────────────────────────────────────────────────────
        composable(
            route = "gameover/{score}",
            arguments = listOf(navArgument("score") { type = NavType.IntType }),
            enterTransition = {
                slideInVertically(tween(420)) { it } + fadeIn(tween(420))
            },
            exitTransition  = {
                slideOutVertically(tween(300)) { it } + fadeOut(tween(300))
            },
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            GameOverScreen(
                navController = navController,
                score         = score,
                viewModel     = gameViewModel,
            )
        }
    }
}
