package com.digiteka.videofeed.ui.carousel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup.LayoutParams
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.digiteka.videofeed.ui.VideoFeedActivity
import com.digiteka.videofeed.utils.ConsentUtils
import com.digiteka.videofeed.utils.UrlUtils
import java.nio.charset.Charset
import kotlin.math.abs

/**
 * A carousel displaying thumbnails of the videos in the feed associated to an MDTK.
 */
@SuppressLint("SetJavaScriptEnabled")
public class VideoFeedCarousel(context: Context, attrs: AttributeSet? = null) : WebView(context, attrs) {

	public companion object {
		internal const val HTML_TEMPLATE_FILENAME = "video_feed_carousel.html"
	}

	/**
	 * Callback to be called when a video is clicked.
	 * If null, the video will be opened in a new fullscreen activity.
	 */
	public var onVideoClicked: ((mdtk: String, videoId: String) -> Unit)? = null // If null, open video in new fullscreen activity

	/**
	 * Ad unit path to be used when a video is clicked.
	 */
	public var adUnitPath: String? = null

	init {
		settings.apply {
			defaultTextEncodingName = "UTF-8"
			javaScriptEnabled = true
			useWideViewPort = true
			loadWithOverviewMode = true
			domStorageEnabled = true
			mediaPlaybackRequiresUserGesture = false
		}

		// Set transparent background
		setBackgroundColor(0x00000000)
		setLayerType(LAYER_TYPE_HARDWARE, null)

		webViewClient = object : WebViewClient() {
			override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
				val url = request?.url
				if (url == null || !UrlUtils.matchVideoFeedUrl(url)) {
					return super.shouldInterceptRequest(view, request)
				}

				val mdtk = UrlUtils.getMdtk(url)
				val videoId = UrlUtils.getVideoId(url)
				val zoneId = UrlUtils.getZoneId(url)

				onVideoClicked?.let { it(mdtk, videoId) } ?: run {
					context.startActivity(VideoFeedActivity.Companion.newInstance(context, mdtk, videoId, adUnitPath, zoneId))
				}
				return WebResourceResponse("text/plain", "UTF-8", "".byteInputStream(Charset.defaultCharset()))
			}
		}

		webChromeClient = object : WebChromeClient() {
			override fun getDefaultVideoPoster(): Bitmap? {
				return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
			}
		}
	}

	/**
	 * Loads the carousel with the feed associated to the given MDTK.
	 */
	public fun load(mdtk: String, adUnitPath: String?, zoneId : String?) {
		this.adUnitPath = adUnitPath
		val html = buildHtml(mdtk, adUnitPath, zoneId)
		loadDataWithBaseURL("https://videofeed.digiteka.com", html, "text/html", "UTF-8", "")
	}

	private fun buildHtml(mdtk: String, adUnitPath: String?, zoneId: String?): String {
		return context.assets.open(HTML_TEMPLATE_FILENAME).use { input ->
			StringBuilder(input.bufferedReader().use {
				it.readText()
			}).toString()
				.replace("\${mdtk}", mdtk)
				.replace("\${consentString}", ConsentUtils.getConsentString(context))
				.replace("\${adUnitPath}", adUnitPath ?: "")
				.replace("\${zoneId}", zoneId ?: "")
		}
	}

	/**
	 * Navigates the carousel to the next video by simulating a native swipe-left gesture.
	 */
	public fun navigateNext() {
		simulateNativeSwipe(isNext = true)
	}

	/**
	 * Navigates the carousel to the previous video by simulating a native swipe-right gesture.
	 */
	public fun navigatePrevious() {
		simulateNativeSwipe(isNext = false)
	}

	// Native MotionEvent swipe: bypasses isTrusted filtering by going through the normal input pipeline.
	// Short swipe (120px over 200ms) centered on the view to avoid fling-to-end behavior.
	private fun simulateNativeSwipe(isNext: Boolean) {
		val w = width.toFloat()
		val h = height.toFloat()
		if (w == 0f || h == 0f) return

		val y = h / 2f
		val swipeDist = minOf(120f, w * 0.25f)
		val centerX = w / 2f
		val startX = centerX + if (isNext) swipeDist / 2f else -swipeDist / 2f
		val endX   = centerX + if (isNext) -swipeDist / 2f else swipeDist / 2f
		val downTime = SystemClock.uptimeMillis()
		val handler = Handler(Looper.getMainLooper())
		val durationMs = 200L
		val steps = 8

		fun send(action: Int, x: Float, dt: Long) {
			val ev = MotionEvent.obtain(downTime, downTime + dt, action, x, y, 0)
			dispatchTouchEvent(ev)
			ev.recycle()
		}

		send(MotionEvent.ACTION_DOWN, startX, 0)
		repeat(steps) { i ->
			val delay = (i + 1) * (durationMs / steps)
			val x = startX + (endX - startX) * (i + 1) / steps
			handler.postDelayed({ send(MotionEvent.ACTION_MOVE, x, delay) }, delay)
		}
		handler.postDelayed({ send(MotionEvent.ACTION_UP, endX, durationMs) }, durationMs)
	}

	private var startX = 0f
	private var startY = 0f
	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent?): Boolean {
		when (event?.action) {
			MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
				startX = event.x
				startY = event.y
				parent.requestDisallowInterceptTouchEvent(false)
			}

			MotionEvent.ACTION_MOVE -> {
				val deltaX = event.x - startX
				val deltaY = event.y - startY
				if (abs(deltaX) > abs(deltaY)) {
					parent.requestDisallowInterceptTouchEvent(true)
				}

				startX = event.x
				startY = event.y
			}
		}
		return super.onTouchEvent(event)
	}
}

/**
 * Composable that displays a carousel of thumbnails of the videos in the feed associated to an MDTK.
 * @params onVideoClicked Callback to be called when a video is clicked. If null, the video will be opened in a new fullscreen activity.
 */
@Composable
public fun VideoFeedCarousel(
	modifier: Modifier = Modifier,
	mdtk: String,
	adUnitPath: String?,
	zoneId: String?,
	onVideoClicked: ((mdtk: String, videoId: String) -> Unit)? = null
) {
	AndroidView(
		modifier = modifier,
		factory = { context ->
			VideoFeedCarousel(context)
				.apply {
					layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
					this.onVideoClicked = onVideoClicked
					// Ensure transparent background in Composable
					setBackgroundColor(0x00000000)
				}
		},
		update = { view ->
			view.load(mdtk, adUnitPath, zoneId)
		})
}

/**
 * Composable that displays a carousel with previous/next navigation arrow buttons below it.
 * @param onVideoClicked Callback invoked when a video thumbnail is clicked. If null, the video opens in a fullscreen activity.
 */
@Composable
public fun VideoFeedCarouselWithNavigation(
	modifier: Modifier = Modifier,
	mdtk: String,
	adUnitPath: String? = null,
	zoneId: String? = null,
	onVideoClicked: ((mdtk: String, videoId: String) -> Unit)? = null
) {
	AndroidView(
		modifier = modifier,
		factory = { context ->
			VideoFeedCarouselView(context).apply {
				layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
				this.onVideoClicked = onVideoClicked
			}
		},
		update = { view ->
			view.load(mdtk, adUnitPath, zoneId)
		}
	)
}
