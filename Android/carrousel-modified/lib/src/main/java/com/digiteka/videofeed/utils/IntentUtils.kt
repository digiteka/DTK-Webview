package com.digiteka.videofeed.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

internal object IntentUtils {
	fun openBrowser(context: Context, url: String) {
		try {
			val webIntent = Intent(Intent.ACTION_VIEW)
			webIntent.setData(Uri.parse(url))
			context.startActivity(webIntent)
		} catch (e: ActivityNotFoundException) {
			Log.w("VideoFeedSDK", "Can't launch browser application for url '$url'", e)
		}
	}

	fun openSharePicker(context: Context, title: String, text: String, url: String) {
		val shareIntent = Intent(Intent.ACTION_SEND)
		shareIntent.type = "text/plain"
		shareIntent.putExtra(Intent.EXTRA_TITLE, title)
		shareIntent.putExtra(Intent.EXTRA_TEXT, "$text $url")
		context.startActivity(Intent.createChooser(shareIntent, null))
	}
}