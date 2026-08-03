package com.digiteka.videofeed

import android.net.Uri

public object VideoFeedConfig {
	public var shouldOverrideUrlLoading : ((uri: Uri) -> Boolean)? = null
}