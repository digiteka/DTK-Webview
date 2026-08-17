package com.example.digitest.ui

import android.app.Activity
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.digitest.DEFAULT_CONSENT_STRING
import com.example.digitest.DEFAULT_VF_MDTK
import com.example.digitest.VideoFeedPreferences
import com.example.digitest.videoFeedFullscreenUrl

// Contourne VideoFeedActivity (SDK fermé) — hardcode videofeed.digiteka.com et n'expose aucun
// paramètre de branche — charge à la place videoFeedFullscreenUrl(), branch-aware (VF_BRANCH).
//
// Attaché directement au decorView (même pattern que fullscreenContainer dans
// InstreamScreen.kt/NoSdkPlayerScreen) plutôt que rendu dans l'arbre Compose : le NavHost de
// MainActivity applique Modifier.padding(innerPadding) (insets Scaffold edge-to-edge) à tous ses
// écrans, ce qui rognerait un WebView plein écran — le decorView, lui, n'a aucun padding.
@Composable
fun VideoFeedFullscreenScreen() {
    val context = LocalContext.current
    val activity = context as Activity
    val config = remember { VideoFeedPreferences.getConfig(context) }

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
                    videoId = config.videoId,
                    zoneId = config.zoneId,
                    vfBranch = config.vfBranch,
                    consentString = if (config.consentStringEnabled) {
                        DEFAULT_CONSENT_STRING
                    } else {
                        ""
                    }
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
