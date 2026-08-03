package com.digiteka.videofeed.sample

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digiteka.videofeed.sample.databinding.CarouselActivityBinding
import com.digiteka.videofeed.ui.carousel.VideoFeedCarousel

class CarouselActivity : AppCompatActivity() {

	companion object {
		private const val EXTRA_MDTK: String = "CarouselActivity.EXTRA_MDTK"
		private const val EXTRA_ADUNITPATH: String = "CarouselActivity.EXTRA_ADUNITPATH"
		private const val EXTRA_ZONE_ID: String = "CarouselActivity.EXTRA_ZONE_ID"
		private const val CAROUSEL_COUNT = 12

		fun newInstance(activity: AppCompatActivity, mdtk: String, adUnitPath: String?, zoneId: String?): Intent {
			return Intent(activity, CarouselActivity::class.java)
				.putExtra(EXTRA_MDTK, mdtk)
				.putExtra(EXTRA_ADUNITPATH, adUnitPath)
				.putExtra(EXTRA_ZONE_ID, zoneId)
		}
	}

	private val mdtk by lazy { checkNotNull(intent.getStringExtra(EXTRA_MDTK)) { "Missing mdtk" } }
	private val adUnitPath by lazy { intent.getStringExtra(EXTRA_ADUNITPATH) }
	private val zoneId by lazy { intent.getStringExtra(EXTRA_ZONE_ID) }

	private val binding: CarouselActivityBinding by lazy {
		CarouselActivityBinding.inflate(layoutInflater)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(binding.root)

		binding.carouselsRecyclerView.apply {
			layoutManager = LinearLayoutManager(this@CarouselActivity)
			adapter = CarouselAdapter(CAROUSEL_COUNT, mdtk, adUnitPath, zoneId)
		}
	}

	private inner class CarouselAdapter(
		private val itemCount: Int,
		private val mdtk: String,
		private val adUnitPath: String?,
		private val zoneId: String?,
	) : RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
			val carousel = VideoFeedCarousel(parent.context).apply {
				layoutParams = ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					resources.getDimensionPixelSize(R.dimen.carousel_height)
				)
			}
			return CarouselViewHolder(carousel)
		}

		override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
			holder.carousel.load(mdtk, adUnitPath, zoneId)

			// Add click listener to the last carousel as an example
			if (position == itemCount - 1) {
				holder.carousel.onVideoClicked = { mdtk, videoId ->
					runOnUiThread {
						Toast.makeText(this@CarouselActivity, "Video clicked: $videoId", Toast.LENGTH_LONG).show()
					}
				}
			}
		}

		override fun getItemCount(): Int = itemCount

		inner class CarouselViewHolder(val carousel: VideoFeedCarousel) : RecyclerView.ViewHolder(carousel)
	}
}
