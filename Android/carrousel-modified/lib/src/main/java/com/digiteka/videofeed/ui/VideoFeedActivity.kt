package com.digiteka.videofeed.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.digiteka.videofeed.databinding.VideoFeedActivityBinding
import com.digiteka.videofeed.utils.UrlUtils

/**
 * An activity containing the VideoFeed associated to an MDTK.
 */
public class VideoFeedActivity() : Activity() {

	public companion object {
		private const val EXTRA_URL: String = "VideoFeedActivity.EXTRA_URL"

		internal fun fromUrl(context: Context, url: String): Intent {
			return Intent(context, VideoFeedActivity::class.java)
				.putExtra(EXTRA_URL, url)
		}

		/**
		 * Create intent to a new `VideoFeedActivity` with the given MDTK and videoId.
		 * @param videoId Identifier of the video displayed first. If null, the first video of the feed will be displayed.
		 * @param adUnitPath Path to the ad unit (/{networkCode}/{adBlockPath})
		 */
		public fun newInstance(context: Context, mdtk: String, videoId: String?, adUnitPath: String?, zoneId: String?): Intent {
			return fromUrl(context, UrlUtils.buildVideoFeedUrl(mdtk, videoId, adUnitPath, zoneId, true))
		}
	}

	private val videoFeedUrl by lazy {
		checkNotNull(intent.getStringExtra(EXTRA_URL)) { "Missing url" }
	}

	private lateinit var binding: VideoFeedActivityBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = VideoFeedActivityBinding.inflate(layoutInflater)
		setContentView(binding.root)

		ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
			val innerPadding = insets.getInsets(
				WindowInsetsCompat.Type.systemBars()
						or WindowInsetsCompat.Type.displayCutout()
			)
			v.setPadding(0, innerPadding.top, 0, innerPadding.bottom)
			insets
		}

		binding.videoFeedView.load(videoFeedUrl) {
			finish()
		}
	}
}