package uz.angrykitten.catloop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import uz.angrykitten.catloop.navigation.NavGraph
import uz.angrykitten.catloop.ui.theme.CatLoopColors
import uz.angrykitten.catloop.ui.theme.CatLoopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatLoopTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = CatLoopColors.Background,
                ) {
                    NavGraph()
                }
            }
        }
    }
}