package com.digiteka.videofeed.sample

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.digiteka.videofeed.ui.carousel.VideoFeedCarousel

class CarouselComposeActivity : ComponentActivity() {

	companion object {
		private const val EXTRA_MDTK: String = "CarouselComposeActivity.EXTRA_MDTK"
		private const val EXTRA_ADUNITPATH: String = "CarouselComposeActivity.EXTRA_ADUNITPATH"
		private const val EXTRA_ZONE_ID: String = "CarouselComposeActivity.EXTRA_ZONE_ID"

		fun newInstance(activity: AppCompatActivity, mdtk: String, adUnitPath: String?, zoneId: String?): Intent {
			return Intent(activity, CarouselComposeActivity::class.java)
				.putExtra(EXTRA_MDTK, mdtk)
				.putExtra(EXTRA_ADUNITPATH, adUnitPath)
				.putExtra(EXTRA_ZONE_ID, zoneId)
		}
	}

	private val mdtk by lazy { checkNotNull(intent.getStringExtra(EXTRA_MDTK)) { "Missing mdtk" } }
	private val adUnitPath by lazy { intent.getStringExtra(EXTRA_ADUNITPATH) }
	private val zoneId by lazy { intent.getStringExtra(EXTRA_ZONE_ID) }

	private val onVideoClicked = { mdtk: String, videoId: String ->
		runOnUiThread {
			Toast.makeText(this, "Video clicked: $videoId", Toast.LENGTH_LONG).show()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			Content(mdtk, adUnitPath, zoneId, onVideoClicked)
		}
	}
}

@Composable
fun Content(mdtk: String, adUnitPath: String?, zoneId: String?, onVideoClicked: (mdtk: String, videoId: String) -> Unit = { _, _ -> }) {
	// Create a list of items to display in the LazyColumn
	// First item doesn't have onVideoClicked callback
	val items = IntRange(0, 12).toList()

	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
			.windowInsetsPadding(WindowInsets.navigationBars),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.SpaceEvenly
	) {
		items(
			items = items,
			key = { index -> index } // Add a unique key for each item to prevent recycling
		) { index ->
			if (index == 0) {
				VideoFeedCarousel(
					mdtk = mdtk,
					adUnitPath = adUnitPath,
					zoneId = zoneId,
					modifier = Modifier
						.fillMaxWidth()
						.height(320.dp),
					onVideoClicked = onVideoClicked
				)
			} else {
				VideoFeedCarousel(
					mdtk = mdtk,
					adUnitPath = adUnitPath,
					zoneId = zoneId,
					modifier = Modifier
						.fillMaxWidth()
						.height(320.dp),
				)
			}
		}
	}
}
