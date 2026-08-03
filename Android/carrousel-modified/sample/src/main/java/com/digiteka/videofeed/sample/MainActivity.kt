package com.digiteka.videofeed.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.digiteka.videofeed.sample.databinding.MainActivityBinding
import com.digiteka.videofeed.ui.VideoFeedActivity

class MainActivity : AppCompatActivity() {

	private val mdtk by lazy { BuildConfig.DIGITEKA_VIDEOFEED_MDTK }
	private val videoId by lazy { BuildConfig.DIGITEKA_VIDEOFEED_VIDEOID }
	private val adUnitPath by lazy { BuildConfig.DIGITEKA_VIDEOFEED_ADUNITPATH }
	private val zoneId by lazy { BuildConfig.DIGITEKA_VIDEOFEED_ZONEID }

	private val binding: MainActivityBinding by lazy {
		MainActivityBinding.inflate(layoutInflater)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(binding.root)

		with(binding) {
			etMdtk.setText(mdtk)
			etVideoId.setText(videoId)
			etAdUnitPath.setText(adUnitPath)
			etZoneId.setText(zoneId)
			btnActivity.setOnClickListener {
				if (etMdtk.text.toString().isEmpty()) etMdtk.error = "MDTK is required"
				else {
					etMdtk.error = null
					startActivity(VideoFeedActivity.newInstance(this@MainActivity, etMdtk.text.toString(), etVideoId.text.toString().nullIfBlank(), etAdUnitPath.text.toString().nullIfBlank(), etZoneId.text.toString().nullIfBlank()))
				}
			}
			btnFragment.setOnClickListener {
				if (etMdtk.text.toString().isEmpty()) etMdtk.error = "MDTK is required"
				else {
					etMdtk.error = null
					startActivity(VideoFeedFragmentActivity.newInstance(this@MainActivity, etMdtk.text.toString(), etVideoId.text.toString().nullIfBlank(), etAdUnitPath.text.toString().nullIfBlank(), etZoneId.text.toString().nullIfBlank()))
				}
			}
			btnCarousel.setOnClickListener {
				if (etMdtk.text.toString().isEmpty()) etMdtk.error = "MDTK is required"
				else {
					etMdtk.error = null
					startActivity(CarouselActivity.newInstance(this@MainActivity, etMdtk.text.toString(), etAdUnitPath.text.toString().nullIfBlank(), etZoneId.text.toString().nullIfBlank()))
				}
			}
			btnCarouselCompose.setOnClickListener {
				if (etMdtk.text.toString().isEmpty()) etMdtk.error = "MDTK is required"
				else {
					etMdtk.error = null
					startActivity(CarouselComposeActivity.newInstance(this@MainActivity, etMdtk.text.toString(), etAdUnitPath.text.toString().nullIfBlank(), etZoneId.text.toString().nullIfBlank()))
				}
			}
			btnVideoPlayer.setOnClickListener {
				startActivity(VideoPlayerActivity.newInstance(this@MainActivity, etMdtk.text.toString(), etAdUnitPath.text.toString().nullIfBlank()))
			}
		}
	}
}