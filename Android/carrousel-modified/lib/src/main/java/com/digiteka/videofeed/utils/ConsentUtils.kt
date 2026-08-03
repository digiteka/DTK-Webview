package com.digiteka.videofeed.utils

import android.content.Context
import android.net.Uri
import androidx.preference.PreferenceManager

internal object ConsentUtils {

	private const val CONSENT_STRING = "IABTCF_TCString"

	fun getConsentString(context: Context): String {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(CONSENT_STRING, "") ?: ""
	}

	fun String.appendUrlConsentString(context: Context): String {
		return Uri.parse(this).buildUpon()
			.appendQueryParameter("gdprconsentstring", getConsentString(context))
			.build()
			.toString()
	}
}