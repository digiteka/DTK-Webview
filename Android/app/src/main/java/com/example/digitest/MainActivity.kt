package com.example.digitest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.digitest.consent.ConsentManager
import com.example.digitest.ui.CarrouselScreen
import com.example.digitest.ui.CookieManagerScreen
import com.example.digitest.ui.HomeScreen
import com.example.digitest.ui.InstreamArticleScreen
import com.example.digitest.ui.InstreamConfigScreen
import com.example.digitest.ui.NoSdkPlayerScreen
import com.example.digitest.ui.VideoFeedConfigScreen
import com.example.digitest.ui.theme.DIGITESTTheme

class MainActivity : ComponentActivity() {

    private var consentReady by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Android 15 (targetSdk 35) forces edge-to-edge — declare it explicitly
        // and let the Scaffold consume the system bar insets.
        enableEdgeToEdge()

        ConsentManager.requestConsent(this) {
            consentReady = true
        }

        setContent {
            DIGITESTTheme {
                if (consentReady) {
                    val navController = rememberNavController()
                    Scaffold { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "home",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("home") { HomeScreen(navController) }
                            composable("instream") { InstreamArticleScreen() }
                            composable("carrousel") { CarrouselScreen() }
                            composable("videofeed_config") { VideoFeedConfigScreen() }
                            composable("nosdk") { NoSdkPlayerScreen() }
                            composable("instream_config") { InstreamConfigScreen() }
                            composable("cookies") { CookieManagerScreen() }
                        }
                    }
                }
            }
        }
    }
}
