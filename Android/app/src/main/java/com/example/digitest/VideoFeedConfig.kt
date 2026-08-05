package com.example.digitest

import android.content.Context

private const val PREFS_NAME = "videofeed_prefs"
private const val KEY_MDTK = "vf_mdtk"
private const val KEY_ZONE_ID = "vf_zone_id"
private const val KEY_AD_UNIT_PATH = "vf_ad_unit_path"
private const val KEY_VIDEO_ID = "vf_video_id"
private const val KEY_CARROUSEL_HEIGHT_VH = "vf_carrousel_height_vh"
private const val KEY_VF_BRANCH = "vf_branch"
private const val KEY_CARR_BRANCH = "carr_branch"

const val DEFAULT_VF_MDTK = "01573101"
const val DEFAULT_VF_CARROUSEL_HEIGHT_VH = "95"

data class VideoFeedConfig(
    val mdtk: String?,
    val zoneId: String?,
    val adUnitPath: String?,
    val videoId: String?,
    val carrouselHeightVh: String?,
    val vfBranch: String?,
    val carrBranch: String?
)

object VideoFeedPreferences {

    fun getConfig(context: Context): VideoFeedConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return VideoFeedConfig(
            mdtk = prefs.getString(KEY_MDTK, null)?.ifEmpty { null },
            zoneId = prefs.getString(KEY_ZONE_ID, null)?.ifEmpty { null },
            adUnitPath = prefs.getString(KEY_AD_UNIT_PATH, null)?.ifEmpty { null },
            videoId = prefs.getString(KEY_VIDEO_ID, null)?.ifEmpty { null },
            carrouselHeightVh = prefs.getString(KEY_CARROUSEL_HEIGHT_VH, null)?.ifEmpty { null },
            vfBranch = prefs.getString(KEY_VF_BRANCH, null)?.ifEmpty { null },
            carrBranch = prefs.getString(KEY_CARR_BRANCH, null)?.ifEmpty { null }
        )
    }

    fun saveConfig(context: Context, config: VideoFeedConfig) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_MDTK, config.mdtk)
            .putString(KEY_ZONE_ID, config.zoneId)
            .putString(KEY_AD_UNIT_PATH, config.adUnitPath)
            .putString(KEY_VIDEO_ID, config.videoId)
            .putString(KEY_CARROUSEL_HEIGHT_VH, config.carrouselHeightVh)
            .putString(KEY_VF_BRANCH, config.vfBranch)
            .putString(KEY_CARR_BRANCH, config.carrBranch)
            .apply()
    }

    fun resetConfig(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .remove(KEY_MDTK)
            .remove(KEY_ZONE_ID)
            .remove(KEY_AD_UNIT_PATH)
            .remove(KEY_VIDEO_ID)
            .remove(KEY_CARROUSEL_HEIGHT_VH)
            .remove(KEY_VF_BRANCH)
            .remove(KEY_CARR_BRANCH)
            .apply()
    }
}
