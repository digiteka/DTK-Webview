package com.digiteka.videofeed.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.digiteka.videofeed.sample.databinding.VideofeedFragmentActivityBinding
import com.digiteka.videofeed.ui.VideoFeedFragment

class VideoFeedFragmentActivity() : AppCompatActivity() {

	companion object {
		private const val EXTRA_MDTK: String = "VideoFeedFragmentActivity.EXTRA_MDTK"
		private const val EXTRA_VIDEO_ID: String = "VideoFeedFragmentActivity.EXTRA_VIDEO_ID"
		private const val EXTRA_ADUNITPATH: String = "VideoFeedFragmentActivity.EXTRA_ADUNITPATH"
		private const val EXTRA_ZONE_ID: String = "VideoFeedFragmentActivity.EXTRA_ZONE_ID"

		fun newInstance(activity: AppCompatActivity, mdtk: String, videoId: String?, adUnitPath: String?, zoneId: String?): Intent {
			return Intent(activity, VideoFeedFragmentActivity::class.java)
				.putExtra(EXTRA_MDTK, mdtk)
				.putExtra(EXTRA_VIDEO_ID, videoId)
				.putExtra(EXTRA_ADUNITPATH, adUnitPath)
				.putExtra(EXTRA_ZONE_ID, zoneId)
		}
	}

	private val mdtk by lazy { checkNotNull(intent.getStringExtra(EXTRA_MDTK)) { "Missing mdtk" } }
	private val videoId by lazy { intent.getStringExtra(EXTRA_VIDEO_ID) }
	private val adUnitPath by lazy { intent.getStringExtra(EXTRA_ADUNITPATH) }
	private val zoneId by lazy { intent.getStringExtra(EXTRA_ZONE_ID) }

	private val binding: VideofeedFragmentActivityBinding by lazy {
		VideofeedFragmentActivityBinding.inflate(layoutInflater)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(binding.root)

		ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
			val innerPadding = insets.getInsets(
				WindowInsetsCompat.Type.systemBars()
						or WindowInsetsCompat.Type.displayCutout()
			)
			v.setPadding(0, innerPadding.top, 0, innerPadding.bottom)
			insets
		}

		binding.fragmentContainerView.getFragment<VideoFeedFragment>().apply {
			load(mdtk, videoId, adUnitPath, zoneId) {
				finish()
			}
		}
	}


}