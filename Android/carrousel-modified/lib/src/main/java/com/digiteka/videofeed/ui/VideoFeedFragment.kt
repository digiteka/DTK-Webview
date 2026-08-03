package com.digiteka.videofeed.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import com.digiteka.videofeed.databinding.VideoFeedFragmentBinding
import com.digiteka.videofeed.utils.UrlUtils
import kotlinx.coroutines.launch

/**
 * A fragment containing the VideoFeed
 */
public class VideoFeedFragment() : Fragment() {

	private var _binding: VideoFeedFragmentBinding? = null
	private val binding get() = _binding!!

	/**
	 * @suppress
	 */
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		_binding = VideoFeedFragmentBinding.inflate(inflater, container, false)
		return binding.root
	}

	/**
	 * Loads the VideoFeed with the given MDTK and videoId.
	 * @param videoId Identifier of the video displayed first. If null, the first video of the feed will be displayed.
	 * @param adUnitPath Path to the ad unit (/{networkCode}/{adBlockPath})
	 * @param zoneId Zone ID to be used for the feed, if applicable.
	 */
	public fun load(mdtk: String, videoId: String?, adUnitPath: String?, zoneId: String?, onCloseClicked: (() -> Unit)? = null) {
		lifecycleScope.launch {
			withStarted {
				binding.videoFeedView.load(mdtk, videoId, adUnitPath, zoneId, onCloseClicked)
			}
		}
	}
}