package com.digiteka.videofeed.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.digiteka.videofeed.VideoFeedConfig
import com.digiteka.videofeed.utils.ConsentUtils.appendUrlConsentString
import com.digiteka.videofeed.utils.IntentUtils
import com.digiteka.videofeed.utils.UrlUtils

@SuppressLint("SetJavaScriptEnabled")
internal class VideoFeedView(context: Context, attrs: AttributeSet? = null) : WebView(context, attrs) {

	private var onCloseClicked: (() -> Unit)? = null

	companion object {
		const val JS_SHARE_FILENAME = "share.js"
	}

	init {
		settings.apply {
			defaultTextEncodingName = "UTF-8"
			javaScriptEnabled = true
			useWideViewPort = true
			loadWithOverviewMode = true
			domStorageEnabled = true
		}

		addJavascriptInterface(AndroidJSInterface(context, { onCloseClicked?.invoke() }), "Android")

		webViewClient = object : WebViewClient() {
			override fun onPageFinished(view: WebView?, url: String?) {
				super.onPageFinished(view, url)
				context.assets.open(JS_SHARE_FILENAME).bufferedReader().use {
					view?.loadUrl("javascript:${it.readText()}")
				}
			}

			override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
				if (request?.url != null) {
					if (VideoFeedConfig.shouldOverrideUrlLoading?.invoke(request.url) == true) {
						return true // URL handled by callback
					} else {
						IntentUtils.openBrowser(context, request.url.toString())
						return true
					}
				}
				return super.shouldOverrideUrlLoading(view, request)
			}
		}

		webChromeClient = object : WebChromeClient() {
			override fun getDefaultVideoPoster(): Bitmap? {
				return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
			}
		}
	}

	internal fun load(url: String, onCloseClicked: (() -> Unit)?) {
		this.onCloseClicked = onCloseClicked
		loadUrl(url.appendUrlConsentString(context))
	}

	internal fun load(mdtk: String, videoId: String?, adUnitPath: String?, zoneId: String?, onCloseClicked: (() -> Unit)?) {
		load(UrlUtils.buildVideoFeedUrl(mdtk, videoId, adUnitPath, zoneId, onCloseClicked != null), onCloseClicked)
	}

	class AndroidJSInterface(private val context: Context, private val onCloseClicked: (() -> Unit)? = null) {
		@JavascriptInterface
		fun androidShare(title: String, text: String, url: String) {
			IntentUtils.openSharePicker(context, title, text, url)
		}

		@JavascriptInterface
		fun videoFeedClose() {
			onCloseClicked?.invoke()
		}
	}
}

@Composable
internal fun VideoFeedView(
	modifier: Modifier = Modifier,
	mdtk: String,
	videoId: String,
	adUnitPath: String?,
	zoneId: String?,
	onUrlClicked: ((String) -> Boolean)?,
	onCloseClicked: (() -> Unit)?
) {
	AndroidView(
		modifier = modifier,
		factory = { context ->
			VideoFeedView(context)
		},
		update = { view ->
			view.load(mdtk, videoId, adUnitPath, zoneId, onCloseClicked)
		})
}