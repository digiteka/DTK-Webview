package com.example.digitest.utils

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.digiteka.videofeed.ui.carousel.VideoFeedCarousel

private const val TAG = "WebViewUtils"

// URLs used to read cookies from CookieManager (must match how they were stored)
private val COOKIE_LOOKUP = mapOf(
    "https://ultimedia.com" to ".ultimedia.com",
    "https://digiteka.com" to ".digiteka.com"
)

/**
 * Parcourt récursivement la hiérarchie de vues, stoppe et détruit chaque WebView trouvée.
 * À appeler dans onDispose pour couper toutes les requêtes réseau des players SDK.
 */
fun destroyWebViewsRecursively(view: View) {
    if (view is WebView) {
        view.stopLoading()
        view.loadUrl("about:blank")
        view.destroy()
    } else if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            destroyWebViewsRecursively(view.getChildAt(i))
        }
    }
}

/**
 * Parcourt récursivement la hiérarchie de vues et active les cookies tiers
 * sur chaque WebView trouvée.
 */
fun enableThirdPartyCookiesRecursively(view: View) {
    if (view is WebView) {
        CookieManager.getInstance().setAcceptThirdPartyCookies(view, true)
        // Ne pas wrapper le WebViewClient du VideoFeedCarrousel : le SDK gère lui-même
        // la navigation (clic → lancement VideoFeedActivity) via son WebViewClient.
        // Wrapper ce client casse silencieusement le clic sur les vignettes.
        if (view !is VideoFeedCarousel) {
            injectCookiesViaJs(view)
        }
    }
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            enableThirdPartyCookiesRecursively(view.getChildAt(i))
        }
    }
}

/**
 * Attache un WebViewClient sur la WebView qui réinjecte les cookies via document.cookie
 * à chaque chargement de page. Contourne le blocage Chrome des cookies tiers (third-party
 * cookie phaseout) en réécrivant les cookies côté JS directement dans le contexte de la page.
 *
 * Le client délègue à l'ancien client pour ne pas casser le comportement existant du SDK.
 */
private fun injectCookiesViaJs(webView: WebView) {
    val previousClient = webView.webViewClient
    webView.webViewClient = object : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: android.webkit.WebResourceRequest
        ): Boolean = previousClient.shouldOverrideUrlLoading(view, request)

        override fun onPageFinished(view: WebView, url: String?) {
            previousClient.onPageFinished(view, url)
            injectCookiesForDomains(view)
        }

        override fun onReceivedError(
            view: WebView,
            request: android.webkit.WebResourceRequest,
            error: android.webkit.WebResourceError
        ) = previousClient.onReceivedError(view, request, error)

        override fun onReceivedSslError(
            view: WebView,
            handler: android.webkit.SslErrorHandler,
            error: android.net.http.SslError
        ) = previousClient.onReceivedSslError(view, handler, error)
    }
}

private fun injectCookiesForDomains(webView: WebView) {
    val cm = CookieManager.getInstance()
    for ((url, domain) in COOKIE_LOOKUP) {
        val cookieString = cm.getCookie(url) ?: continue
        // cookieString = "name1=val1; name2=val2; ..."
        val cookies = cookieString.split(";").map { it.trim() }.filter { it.isNotEmpty() }
        if (cookies.isEmpty()) continue

        val js = buildString {
            append("(function(){")
            for (cookie in cookies) {
                // Inject each cookie with SameSite=None; Secure so Chromium accepts it
                val escaped = cookie.replace("\\", "\\\\").replace("'", "\\'")
                append("document.cookie='$escaped; SameSite=None; Secure; domain=$domain; path=/';")
            }
            append("})()")
        }
        webView.evaluateJavascript(js) { result ->
            Log.d(TAG, "Cookie injection for $domain → $result")
        }
    }
}
