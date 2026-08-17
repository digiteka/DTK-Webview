package com.example.digitest

import android.content.Context
import android.net.Uri
import android.util.Log

private const val PREFS_NAME = "videofeed_prefs"
private const val KEY_MDTK = "vf_mdtk"
private const val KEY_ZONE_ID = "vf_zone_id"
private const val KEY_AD_UNIT_PATH = "vf_ad_unit_path"
private const val KEY_VIDEO_ID = "vf_video_id"
private const val KEY_CARROUSEL_HEIGHT_VH = "vf_carrousel_height_vh"
private const val KEY_VF_BRANCH = "vf_branch"
private const val KEY_CARR_BRANCH = "carr_branch"
private const val KEY_CONSENT_STRING_ENABLED = "vf_consent_string_enabled"
private const val VF_AMPLIFY_APP_ID = "d33gpayci0qt6k"

const val DEFAULT_VF_MDTK = "01573101"
const val DEFAULT_VF_CARROUSEL_HEIGHT_VH = "95"

/**
 * Host servant le VideoFeed — bascule vers la preview Amplify de la branche VF_BRANCH si renseignée,
 * ou vers un serveur dev LAN si VF_BRANCH est une IP (même convention que preprodUrl() côté
 * verticalvideos/src/launcher/debug.ts:17-29).
 */
fun videoFeedHost(vfBranch: String?): String {
    if (vfBranch.isNullOrEmpty()) return "videofeed.digiteka.com"
    if (vfBranch.startsWith("192.168")) return vfBranch
    return "$vfBranch.$VF_AMPLIFY_APP_ID.amplifyapp.com"
}

/**
 * URL de la page VideoFeed plein écran — même format que la branche isMobileApp de
 * openVideofeed() (verticalvideos/src/launcher/_open.ts:59-97), mais avec videoFeedHost()
 * à la place de videofeed_domain. Nécessaire car VideoFeedActivity (SDK fermé) charge en dur
 * videofeed.digiteka.com et n'expose aucun paramètre de branche.
 */
fun videoFeedFullscreenUrl(
    mdtk: String,
    videoId: String?,
    zoneId: String?,
    vfBranch: String?,
    consentString: String?,
    source: String? = null
): String {
    val builder = Uri.parse("https://${videoFeedHost(vfBranch)}/").buildUpon()
        .appendQueryParameter("mdtk", mdtk)
        .appendQueryParameter("debug", "1")
    if (!videoId.isNullOrEmpty()) builder.appendQueryParameter("video_id", videoId)
    if (!zoneId.isNullOrEmpty()) builder.appendQueryParameter("vf_zone_index", zoneId)
    if (!consentString.isNullOrEmpty()) builder.appendQueryParameter("gdprconsentstring", consentString)
    if (!source.isNullOrEmpty()) builder.appendQueryParameter("source", source)
    return builder.build().toString()
}

data class VideoFeedConfig(
    val mdtk: String?,
    val zoneId: String?,
    val adUnitPath: String?,
    val videoId: String?,
    val carrouselHeightVh: String?,
    val vfBranch: String?,
    val carrBranch: String?,
    val consentStringEnabled: Boolean
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
            carrBranch = prefs.getString(KEY_CARR_BRANCH, null)?.ifEmpty { null },
            consentStringEnabled = prefs.getBoolean(KEY_CONSENT_STRING_ENABLED, false)
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
            .putBoolean(KEY_CONSENT_STRING_ENABLED, config.consentStringEnabled)
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
            .remove(KEY_CONSENT_STRING_ENABLED)
            .apply()
    }
}
