package com.digiteka.videofeed.sample

fun String.nullIfBlank(): String? {
	return if (isBlank()) null else this
}