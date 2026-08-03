package com.example.digitest

import android.content.Context
import com.digiteka.instream.core.config.PlayMode

private const val SHARED_PREFS = "instream_shared_prefs"
private const val SDK_PREFS = "instream_sdk_prefs"
private const val NOSDK_PREFS = "instream_nosdk_prefs"

// Paramètres communs aux deux intégrations
const val DEFAULT_IS_MDTK = "01850262"
const val DEFAULT_IS_ZONE = "1"
const val DEFAULT_IS_SRC = "383kuzf"
const val DEFAULT_IS_URL_REFERRER = "https://test-app.digiteka.com/article"

// Paramètre spécifique SDK
const val DEFAULT_IS_PLAY_MODE = "ON_CLICK"

data class InstreamSharedConfig(
    val mdtk: String?,
    val zone: String?,
    val src: String?,
    val urlReferrer: String?
)

data class InstreamSdkConfig(
    val mdtk: String?,
    val zone: String?,
    val src: String?,
    val urlReferrer: String?,
    val playMode: String? // "ON_CLICK" | "VISIBLE_AT_FIFTY_PERCENT" | "AUTOPLAY"
)

data class InstreamNoSdkConfig(
    val mdtk: String?,
    val zone: String?,
    val src: String?,
    val urlReferrer: String?,
    val chromeless: Boolean,
    val customUrl: String?
)

fun playModeFromString(name: String?): PlayMode = when (name) {
    "VISIBLE_AT_FIFTY_PERCENT" -> PlayMode.VISIBLE_AT_FIFTY_PERCENT
    "AUTOPLAY" -> PlayMode.AUTOPLAY
    else -> PlayMode.ON_CLICK
}

object InstreamPreferences {

    fun getSharedConfig(context: Context): InstreamSharedConfig {
        val p = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        return InstreamSharedConfig(
            mdtk = p.getString("mdtk", null)?.ifEmpty { null },
            zone = p.getString("zone", null)?.ifEmpty { null },
            src = p.getString("src", null)?.ifEmpty { null },
            urlReferrer = p.getString("url_referrer", null)?.ifEmpty { null }
        )
    }

    fun saveSharedConfig(context: Context, config: InstreamSharedConfig) {
        context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE).edit()
            .putString("mdtk", config.mdtk)
            .putString("zone", config.zone)
            .putString("src", config.src)
            .putString("url_referrer", config.urlReferrer)
            .apply()
    }

    fun resetSharedConfig(context: Context) {
        context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun getSdkConfig(context: Context): InstreamSdkConfig {
        val shared = getSharedConfig(context)
        val p = context.getSharedPreferences(SDK_PREFS, Context.MODE_PRIVATE)
        return InstreamSdkConfig(
            mdtk = shared.mdtk,
            zone = shared.zone,
            src = shared.src,
            urlReferrer = shared.urlReferrer,
            playMode = p.getString("play_mode", null)?.ifEmpty { null }
        )
    }

    fun saveSdkConfig(context: Context, playMode: String?) {
        context.getSharedPreferences(SDK_PREFS, Context.MODE_PRIVATE).edit()
            .putString("play_mode", playMode)
            .apply()
    }

    fun resetSdkConfig(context: Context) {
        context.getSharedPreferences(SDK_PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun getNoSdkConfig(context: Context): InstreamNoSdkConfig {
        val shared = getSharedConfig(context)
        val p = context.getSharedPreferences(NOSDK_PREFS, Context.MODE_PRIVATE)
        return InstreamNoSdkConfig(
            mdtk = shared.mdtk,
            zone = shared.zone,
            src = shared.src,
            urlReferrer = shared.urlReferrer,
            chromeless = p.getBoolean("chromeless", false),
            customUrl = p.getString("custom_url", null)?.ifEmpty { null }
        )
    }

    fun saveNoSdkConfig(context: Context, chromeless: Boolean, customUrl: String?) {
        context.getSharedPreferences(NOSDK_PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("chromeless", chromeless)
            .putString("custom_url", customUrl)
            .apply()
    }

    fun resetNoSdkConfig(context: Context) {
        context.getSharedPreferences(NOSDK_PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
