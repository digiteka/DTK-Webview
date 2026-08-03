package com.digiteka.videofeed.sample

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.digiteka.videofeed.sample.databinding.VideoPlayerActivityBinding
import com.digiteka.videofeed.ui.VideoFeedActivity

class VideoPlayerActivity : AppCompatActivity() {

	companion object {
		private const val EXTRA_MDTK: String = "CarouselActivity.EXTRA_MDTK"
		private const val EXTRA_ADUNITPATH: String = "CarouselActivity.EXTRA_ADUNITPATH"
		private const val EXTRA_ZONEID: String = "CarouselActivity.EXTRA_ZONEID"

		private val iframe = "<iframe " +
				"	src=\"https://www.ultimedia.com/deliver/generic/iframe/mdtk/01857682/zone/1/showtitle/1/src/xkx5zlf/chromeless/1\" " +
				"	width=\"100%\" " +
				"	height=\"100%\" " +
				"	frameborder=\"0\" " +
				"	scrolling=\"no\" " +
				"	marginwidth=\"0\" " +
				"	marginheight=\"0\" " +
				"	hspace=\"0\" " +
				"	vspace=\"0\" " +
				"	webkitallowfullscreen=\"true\" " +
				"	mozallowfullscreen=\"true\" " +
				"	allowfullscreen=\"true\" " +
				"	referrerpolicy=\"no-referrer-when-downgrade\"> " +
				"</iframe>"

		private val postMessageListener = "window.addEventListener('message', function(e) { " +
				"	AndroidJavascriptInterface.postMessage(e.data); " +
				"});"

		fun newInstance(context: Context, mdtk: String, adUnitPath: String?): Intent {
			return Intent(context, VideoPlayerActivity::class.java)
				.putExtra(EXTRA_MDTK, mdtk)
				.putExtra(EXTRA_ADUNITPATH, adUnitPath)
		}
	}

	private val binding: VideoPlayerActivityBinding by lazy {
		VideoPlayerActivityBinding.inflate(layoutInflater)
	}

	private val mdtk by lazy { checkNotNull(intent.getStringExtra(EXTRA_MDTK)) { "Missing mdtk" } }
	private val adUnitPath by lazy { intent.getStringExtra(EXTRA_ADUNITPATH) }
	private val zoneId by lazy { intent.getStringExtra(EXTRA_ZONEID) }

	@SuppressLint("SetJavaScriptEnabled")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(binding.root)

		with(binding.videoPlayer) {
			settings.apply {
				defaultTextEncodingName = "UTF-8"
				javaScriptEnabled = true
				useWideViewPort = true
				loadWithOverviewMode = true
				domStorageEnabled = true
			}

			addJavascriptInterface(AndroidJavascriptInterface(context, mdtk, adUnitPath, zoneId), "AndroidJavascriptInterface")
			webViewClient = object : WebViewClient() {
				override fun onPageFinished(view: WebView?, url: String?) {
					super.onPageFinished(view, url)
					view?.loadUrl("javascript:$postMessageListener")
				}
			}

			loadData(iframe, "text/html", "UTF-8")
		}

	}

	class AndroidJavascriptInterface(
		private val context: Context,
		private val mdtk: String,
		private val adUnitPath: String?,
		private val zoneId: String?
	) {
		@JavascriptInterface
		fun postMessage(data: String) {
			if (data.contains("trigger_vf_chromeless")) {
				data.split("-").lastOrNull()?.let { videoId ->
					context.startActivity(VideoFeedActivity.newInstance(context, mdtk, videoId, adUnitPath, zoneId))
				}
			}
		}
	}

}