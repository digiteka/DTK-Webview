package com.digiteka.videofeed.utils

import android.net.Uri

internal object UrlUtils {

	fun matchVideoFeedUrl(uri: Uri): Boolean {
		return uri.host == "videofeed.digiteka.com"
				&& uri.getQueryParameter("source") == "carrousel"
				&& !uri.getQueryParameter("video_id").isNullOrBlank()
	}

	fun getMdtk(uri: Uri): String {
		return checkNotNull(uri.getQueryParameter("mdtk")) { "Missing mdtk" }
	}

	fun getVideoId(uri: Uri): String {
		return checkNotNull(uri.getQueryParameter("video_id")) { "Missing video_id" }
	}

	fun getZoneId(uri: Uri): String? {
		return uri.getQueryParameter("vf_zone_index")
	}

	fun buildVideoFeedUrl(mdtk: String, videoId: String?, adUnitPath: String?, zoneId :String?, closeButton: Boolean): String {
		return "https://videofeed.digiteka.com/?mdtk=$mdtk&source=carrousel&fromsdk=1" +
				(videoId?.let { "&video_id=$it" } ?: "") +
				(adUnitPath?.let { "&vf_adunit_path=$it" } ?: "") +
				(zoneId?.let { "&vf_zone_index=$it" } ?: "") +
				(if (closeButton) "&close_button=1" else "")
	}
}