package com.example.digitest.ui

import android.app.Activity
import android.net.Uri
import android.net.http.SslError
import android.util.Log
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
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
import androidx.navigation.NavController
import com.digiteka.videofeed.ui.VideoFeedActivity
import com.digiteka.videofeed.ui.carousel.VideoFeedCarousel
import com.example.digitest.DEFAULT_VF_CARROUSEL_HEIGHT_VH
import com.example.digitest.DEFAULT_VF_MDTK
import com.example.digitest.VideoFeedPreferences
import com.example.digitest.consent.ConsentManager
import com.example.digitest.utils.destroyWebViewsRecursively
import com.example.digitest.utils.enableThirdPartyCookiesRecursively
import com.example.digitest.videoFeedFullscreenUrl
import org.json.JSONObject
import java.io.ByteArrayInputStream

@Composable
fun CarrouselScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as Activity
    val config = remember { VideoFeedPreferences.getConfig(context) }
    val carrouselView = remember {
        VideoFeedCarousel(context).also {
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // Pont JS -> natif : la page (video_feed_carrousel.html) émet un window.postMessage
            // "openFromCarrousel" au clic sur une vignette. Si VF_BRANCH est renseigné, on ouvre
            // nous-mêmes VideoFeedFullscreenScreen (branch-aware) plutôt que de compter sur
            // l'interception réseau ci-dessous (previewVideofeedHost, qui n'attrape que les
            // VF_BRANCH déjà au format host Amplify complet).
            if (!config.vfBranch.isNullOrBlank()) {
                it.addJavascriptInterface(
                    OpenFromCarrouselBridge(activity, navController),
                    "AndroidVfBridge"
                )
            }
            // Le SDK de prod n'expose ni placeholder ni API publique pour MDTK_carrousel_height
            // (en dur à "95vh" dans son propre template) : on reconstruit le HTML nous-mêmes,
            // à partir du même asset "video_feed_carrousel.html" livré par le SDK, pour pouvoir
            // le surcharger. Le WebViewClient du SDK (interception des clics vignette) reste
            // attaché puisqu'il est posé dans le constructeur de VideoFeedCarrousel.
            val html = buildCarrouselHtml(
                context = context,
                mdtk = config.mdtk ?: DEFAULT_VF_MDTK,
                adUnitPath = config.adUnitPath,
                zoneId = config.zoneId,
                heightVh = config.carrouselHeightVh ?: DEFAULT_VF_CARROUSEL_HEIGHT_VH,
                vfBranch = config.vfBranch,
                carrBranch = config.carrBranch
            )
            it.loadDataWithBaseURL("https://videofeed.digiteka.com", html, "text/html", "UTF-8", "")

            // Le SDK (VideoFeedCarousel$WebViewClient) détecte l'ouverture du VideoFeed en
            // comparant en dur l'host de la requête interceptée à "videofeed.digiteka.com"
            // (aucune allowlist configurable dans l'AAR compilé). Pour tester avec un domaine
            // de preview Amplify (ex: vfBranch = "evo-459.d5obtkx4de1s5.amplifyapp.com"), on
            // duplique l'interception ici en délégant tout le reste au client SDK original —
            // sinon on casse silencieusement le clic vignette normal (cf enableThirdPartyCookiesRecursively).
            previewVideofeedHost(config.vfBranch)?.let { previewHost ->
                val sdkWebViewClient = it.webViewClient
                it.webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest
                    ): WebResourceResponse? {
                        val mdtk = request.url.getQueryParameter("mdtk")
                        if (request.url.host == previewHost && mdtk != null) {
                            context.startActivity(
                                VideoFeedActivity.newInstance(
                                    context = context,
                                    mdtk = mdtk,
                                    videoId = request.url.getQueryParameter("video_id"),
                                    adUnitPath = config.adUnitPath,
                                    zoneId = request.url.getQueryParameter("vf_zone_index")
                                )
                            )
                            return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
                        }
                        return sdkWebViewClient.shouldInterceptRequest(view, request)
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                        sdkWebViewClient.shouldOverrideUrlLoading(view, request)

                    override fun onPageFinished(view: WebView, url: String?) =
                        sdkWebViewClient.onPageFinished(view, url)

                    override fun onReceivedError(
                        view: WebView,
                        request: WebResourceRequest,
                        error: WebResourceError
                    ) = sdkWebViewClient.onReceivedError(view, request, error)

                    override fun onReceivedSslError(
                        view: WebView,
                        handler: SslErrorHandler,
                        error: SslError
                    ) = sdkWebViewClient.onReceivedSslError(view, handler, error)
                }
            }
        }
    }

    DisposableEffect(carrouselView) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            enableThirdPartyCookiesRecursively(carrouselView)
        }
        carrouselView.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            carrouselView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
            destroyWebViewsRecursively(carrouselView)
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
                text = "VideoFeed Carrousel",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            AndroidView(
                factory = { carrouselView },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
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

/**
 * Reçoit le postMessage "openFromCarrousel" (émis par video_feed_carrousel.html au clic sur une
 * vignette, avec les infos de la vidéo cliquée en slideInfos) et ouvre
 * VideoFeedCarrouselFullscreenScreen avec ces infos. Les méthodes @JavascriptInterface s'exécutent
 * sur un thread WebView, pas le thread UI — d'où le runOnUiThread avant tout appel navController.
 */
private class OpenFromCarrouselBridge(
    private val activity: Activity,
    private val navController: NavController
) {
    @JavascriptInterface
    fun openFromCarrousel(slideInfosJson: String) {
        activity.runOnUiThread {
            navController.navigate(
                "videofeed_carrousel_fullscreen?slide_infos=${Uri.encode(slideInfosJson)}"
            )
        }
    }
}

/**
 * À l'instar de VideoFeedFullscreenScreen() — WebView attachée directement au decorView (pas de
 * padding Scaffold) — mais construite depuis les slideInfos du clic carrousel plutôt que depuis la
 * config statique, et avec le paramètre "source=carrousel" dans l'url.
 */
@Composable
fun VideoFeedCarrouselFullscreenScreen(slideInfosJson: String?) {
    val context = LocalContext.current
    val activity = context as Activity
    val config = remember { VideoFeedPreferences.getConfig(context) }
    val videoId = remember(slideInfosJson) {
        slideInfosJson
            ?.let { runCatching { JSONObject(it).optString("id", "") }.getOrNull() }
            ?.takeIf { it.isNotBlank() }
    }

    val webView = remember {
        WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                @Suppress("SetJavaScriptEnabled")
                javaScriptCanOpenWindowsAutomatically = true
                useWideViewPort = true
                loadWithOverviewMode = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                cacheMode = WebSettings.LOAD_NO_CACHE
            }
            webViewClient = WebViewClient()
            loadUrl(
                videoFeedFullscreenUrl(
                    mdtk = config.mdtk ?: DEFAULT_VF_MDTK,
                    videoId = videoId ?: config.videoId,
                    zoneId = config.zoneId,
                    vfBranch = config.vfBranch,
                    consentString = ConsentManager.getTcString(context),
                    source = "carrousel"
                )
            )
        }
    }

    DisposableEffect(webView) {
        val decorView = activity.window.decorView as FrameLayout
        decorView.addView(
            webView,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        )
        onDispose { decorView.removeView(webView) }
    }
}

/**
 * Extrait un host de preview Amplify complet depuis vfBranch (ex: "evo-459.d5obtkx4de1s5.amplifyapp.com"
 * ou "https://evo-459.d5obtkx4de1s5.amplifyapp.com"). Retourne null si vfBranch est un simple nom de
 * branche (résolu par le SDK/JS lui-même) ou absent — même heuristique que preprodUrl() côté verticalvideos.
 */
private fun previewVideofeedHost(vfBranch: String?): String? {
    if (vfBranch.isNullOrBlank()) return null
    val withoutProtocol = vfBranch.removePrefix("https://").removePrefix("http://")
    return withoutProtocol.takeIf { it.contains(".amplifyapp.com") }
}

private fun buildCarrouselHtml(
    context: android.content.Context,
    mdtk: String,
    adUnitPath: String?,
    zoneId: String?,
    heightVh: String,
    vfBranch: String?,
    carrBranch: String?
): String {
    val template = context.assets.open("video_feed_carrousel.html").bufferedReader().use { it.readText() }
    return template
        .replace("\${mdtk}", mdtk)
        .replace("\${consentString}", ConsentManager.getTcString(context) ?: "")
        .replace("\${heightVh}", heightVh ?: "")
        .replace("\${adUnitPath}", adUnitPath ?: "")
        .replace("\${zoneId}", zoneId ?: "")
        .replace("\${vfBranch}", vfBranch ?: "")
        .replace("\${carrBranch}", carrBranch ?: "")
}

private const val loremIpsum = """Le composant VideoFeed Carrousel permet d'intégrer facilement un carrousel de vidéos dans n'importe quelle page de votre application.

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.

Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.

Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo."""
