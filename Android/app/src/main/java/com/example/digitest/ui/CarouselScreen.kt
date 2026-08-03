package com.example.digitest.ui

import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.digiteka.videofeed.ui.carousel.VideoFeedCarouselView
import com.example.digitest.DEFAULT_VF_MDTK
import com.example.digitest.VideoFeedPreferences
import com.example.digitest.utils.destroyWebViewsRecursively
import com.example.digitest.utils.enableThirdPartyCookiesRecursively

@Composable
fun CarouselScreen() {
    val context = LocalContext.current
    val config = remember { VideoFeedPreferences.getConfig(context) }
    val carouselView = remember {
        VideoFeedCarouselView(context).also {
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            it.load(
                config.mdtk ?: DEFAULT_VF_MDTK,
                config.adUnitPath,
                config.zoneId
            )
        }
    }

    DisposableEffect(carouselView) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            enableThirdPartyCookiesRecursively(carouselView)
        }
        carouselView.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            carouselView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
            destroyWebViewsRecursively(carouselView)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "VideoFeed Carousel",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            AndroidView(
                factory = { carouselView },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)  // 280dp carousel + ~48dp nav buttons + marge
            )

            Text(
                text = "Intégration VideoFeed",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
            )

            Text(
                text = loremIpsum,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

private const val loremIpsum = """Le composant VideoFeed Carousel permet d'intégrer facilement un carousel de vidéos dans n'importe quelle page de votre application.

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.

Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.

Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo."""
