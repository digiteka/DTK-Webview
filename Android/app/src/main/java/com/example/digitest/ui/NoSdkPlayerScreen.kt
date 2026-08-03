package com.example.digitest.ui

import android.app.Activity
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.digitest.DEFAULT_IS_MDTK
import com.example.digitest.DEFAULT_IS_SRC
import com.example.digitest.DEFAULT_IS_URL_REFERRER
import com.example.digitest.DEFAULT_IS_ZONE
import com.example.digitest.InstreamPreferences
import com.example.digitest.consent.ConsentManager

private const val NO_SDK_BASE_URL = "https://test-app.digiteka.com"

@Composable
fun NoSdkPlayerScreen() {
    val context = LocalContext.current
    val activity = context as Activity
    val tcString = remember { ConsentManager.getTcString(context) ?: "" }
    val cfg = remember { InstreamPreferences.getNoSdkConfig(context) }

    val mdtk = cfg.mdtk ?: DEFAULT_IS_MDTK
    val zone = cfg.zone ?: DEFAULT_IS_ZONE
    val src = cfg.src ?: DEFAULT_IS_SRC
    val urlReferrer = cfg.urlReferrer ?: DEFAULT_IS_URL_REFERRER

    val fullscreenContainer = remember {
        FrameLayout(context).apply { visibility = View.GONE }
    }

    DisposableEffect(fullscreenContainer) {
        val decorView = activity.window.decorView as FrameLayout
        decorView.addView(
            fullscreenContainer,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        onDispose { decorView.removeView(fullscreenContainer) }
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
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            webViewClient = WebViewClient()
            webChromeClient = buildChromeClient(activity, fullscreenContainer)

            val iframeUrl = cfg.customUrl
                ?: buildIframeUrl(tcString, mdtk, zone, src, urlReferrer, cfg.chromeless)
            loadDataWithBaseURL(NO_SDK_BASE_URL, buildArticleHtml(iframeUrl, urlReferrer), "text/html", "UTF-8", null)
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
            text = "Article de presse",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = noSdkParagraph1,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = noSdkParagraph2,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Player sans SDK — iframe ultimedia.com
        AndroidView(
            factory = { webView },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = noSdkParagraph3,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = noSdkParagraph4,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = noSdkParagraph5,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = noSdkParagraph6,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = noSdkParagraph7,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = noSdkParagraph8,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = noSdkParagraph9,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = noSdkParagraph10,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = noSdkParagraph11,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = noSdkParagraph12,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text(
            text = noSdkParagraph13,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        } // Column
    } // Surface
}

private fun buildChromeClient(
    activity: Activity,
    fullscreenContainer: FrameLayout
) = object : WebChromeClient() {
    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null
    private var originalUiVisibility = 0

    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
        if (customView != null) { onHideCustomView(); return }
        customView = view
        customViewCallback = callback
        originalUiVisibility = activity.window.decorView.systemUiVisibility
        fullscreenContainer.addView(
            view,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        fullscreenContainer.visibility = View.VISIBLE
        @Suppress("DEPRECATION")
        activity.window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    override fun onHideCustomView() {
        fullscreenContainer.removeView(customView)
        fullscreenContainer.visibility = View.GONE
        @Suppress("DEPRECATION")
        activity.window.decorView.systemUiVisibility = originalUiVisibility
        customViewCallback?.onCustomViewHidden()
        customView = null
        customViewCallback = null
    }
}

private fun buildIframeUrl(
    tcString: String,
    mdtk: String,
    zone: String,
    src: String,
    urlReferrer: String,
    chromeless: Boolean
) = "https://www.ultimedia.com/deliver/generic/iframe" +
    "/mdtk/$mdtk" +
    "/zone/$zone" +
    "/src/$src" +
    "/showtitle/1" +
    (if (chromeless) "/chromeless/1" else "") +
    "/gdprconsentstring/$tcString" +
    "/urlfacebook=${Uri.encode(urlReferrer)}"

private fun buildArticleHtml(iframeUrl: String, canonicalUrl: String) = """
    <!doctype html><html>
    <head>
      <meta name='viewport' content='width=device-width, initial-scale=1' />
      <link rel='canonical' href='$canonicalUrl'>
      <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { background: #000; }
        .wrapper { position: relative; padding-bottom: 56.25%; height: 0; width: 100%; }
        iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: 0; }
      </style>
    </head>
    <body>
      <div class='wrapper'>
        <iframe
          id='um_ultimedia_wrapper_iframeUltimedia'
          scrolling='no'
          allowfullscreen='true'
          allow='autoplay'
          src='$iframeUrl'>
        </iframe>
      </div>
    </body>
    </html>
""".trimIndent()

private const val noSdkParagraph1 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."

private const val noSdkParagraph2 = "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."

private const val noSdkParagraph3 = "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo."

private const val noSdkParagraph4 = "Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet."

private const val noSdkParagraph5 = "At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident."

private const val noSdkParagraph6 = "Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis."

private const val noSdkParagraph7 = "Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat. Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse."

private const val noSdkParagraph8 = "Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur."

private const val noSdkParagraph9 = "Sed haec quis possit intuentem istius virtutem non admirari? Quae animi magnitudo in tam brevi vita tot res gestas includat! Mihi quidem Antimachus ille Colophoniis, magnus profecto poeta, magnus tamen et clarus orator."

private const val noSdkParagraph10 = "Nos autem de tota hac ratione in Hortensio satis, ut nobis quidem videtur, disputavimus. Sed haec hactenus; nunc ad institutum munus revertemur, ut de finibus bonorum et malorum aliquando dicamus."

private const val noSdkParagraph11 = "Certe, inquam, pertinax non ero tibique, si mihi probabis ea quae dicis, libenter adsentiar. Probabo, inquit, modo ista sis aequitate quam ostendis. Sed quoniam et advesperascit et mihi domum revertendum est."

private const val noSdkParagraph12 = "Quod si ita se habeat, non possit beatam praestare vitam sapientia. Bonum integritas corporis erat, quod nunc in partes dividitur. Ut in geometria, prima si dederis, danda sunt omnia."

private const val noSdkParagraph13 = "Hoc loco tenere se Triarius non potuit. Tum Piso: Atqui, Cicero, inquit, haec in scholis Stoicorum exercitatio est quaedam, ut eos quos probant ea ipsa ex quibus probant refellant. Etenim non satis est illud dicere."
