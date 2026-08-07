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

// Consent string de test — force un TC string fixe au lieu du vrai retour de la CMP
const val DEFAULT_CONSENT_STRING = "CQlo8kAQlo8kAAHABBENCiFsAP_gAEPgAAAALAEB7C_cRSFicSZn4LsgSQxewUhCoMAhBAIIACwBiAIAJJwG1mECIAjAgCAKABIAICRAAQAlCADABAAAAIABITCEIEAQARAAIqBAAAARQgIACAhAGQAAGAAQgMJUAgEAkAMECBqoQFhAAQAgigAQIAAlAICFAAAAAAAgQAAAIAAAAmwQEgAAcAIAEAAAAFEMAAAAoAECAAAAEAAQAAAAQBAAAAAAAAAgAQAIgAQAAAAABBYAgPYX7iKQsTiQI_BdkASGL2CkAVBgEIIBBAASAMQBABJGAWswgRAEQAAQBAAIABASIAAAEIAAIAIAAABAAJCIQgAgCACIAABQIAAACKEAAAAEIASAAAwACEBhKgAAgEAAggANUCAsAACAEEUAABAAAoBAQgAAAAAAECAAABAAAAEyAAkAADgBAAgAAAAIhgAAAFAAAQAAAAgACAAAACAAAAAAAAAAEAAABEACAAAAAAIAwSADAAEGSB0AGAAIMkEIAMAAQZIJQAYAAgyQUgAwABBkgtABgACDJAAA.ILAEB7C_cRSFicSZn4LsgSQxewUhCoMAhBAIIACwBiAIAJJwG1mECIAjAgCAKABIAICRAAQAlCADABAAAAIABITCEIEAQARAAIqBAAAARQgIACAhAGQAAGAAQgMJUAgEAkAMECBqoQFhAAQAgigAQIAAlAICFAAAAAAAgQAAAIAAAAmwQEgAAcAIAEAAAAFEMAAAAoAECAAAAEAAQAAAAQBAAAAAAAAAgAQAIgAQAAAAABAA.f_wAH_wAAAAA"

data class InstreamSharedConfig(
    val mdtk: String?,
    val zone: String?,
    val src: String?,
    val urlReferrer: String?,
    val tagParam: String?,
    val consentStringEnabled: Boolean
)

data class InstreamSdkConfig(
    val mdtk: String?,
    val zone: String?,
    val src: String?,
    val urlReferrer: String?,
    val tagParam: String?,
    val consentStringEnabled: Boolean,
    val playMode: String? // "ON_CLICK" | "VISIBLE_AT_FIFTY_PERCENT" | "AUTOPLAY"
)

data class InstreamNoSdkConfig(
    val mdtk: String?,
    val zone: String?,
    val src: String?,
    val urlReferrer: String?,
    val tagParam: String?,
    val consentStringEnabled: Boolean,
    val chromeless: Boolean,
    val customUrl: String?,
    val newplayerMode: String?,
    val newplayerBranchName: String?,
    val newplayerLocalIP: String?
)

fun playModeFromString(name: String?): PlayMode = when (name) {
    "VISIBLE_AT_FIFTY_PERCENT" -> PlayMode.VISIBLE_AT_FIFTY_PERCENT
    "AUTOPLAY" -> PlayMode.AUTOPLAY
    else -> PlayMode.ON_CLICK
}

// Valeur numérique attendue par le paramètre /autoplay/ de l'iframe ultimedia — null si aucun mode sélectionné
fun autoplayValueFor(playMode: String?): Int? = when (playMode) {
    "AUTOPLAY" -> 1
    "VISIBLE_AT_FIFTY_PERCENT" -> 2
    "ON_CLICK" -> 0
    else -> null
}

fun effectiveConsentString(enabled: Boolean): String = if (enabled) DEFAULT_CONSENT_STRING else ""

// Bascule le player JS chargé par l'iframe ultimedia — indépendant du SDK natif
enum class NewplayerMode(val label: String) {
    LEGACY("Legacy"),
    PROD("New"),
    RECETTE("New : Test"),
    LOCAL("New : Local");

    companion object {
        fun fromString(name: String?): NewplayerMode = entries.find { it.name == name } ?: LEGACY
    }
}

fun resolveNewplayer(mode: String?, branchName: String, localIp: String): String? = when (NewplayerMode.fromString(mode)) {
    NewplayerMode.LEGACY -> null
    NewplayerMode.PROD -> "prod"
    NewplayerMode.RECETTE -> branchName.ifBlank { null }?.let { "https://$it.d2sdl16pluelsx.amplifyapp.com" }
    NewplayerMode.LOCAL -> localIp.ifBlank { null }?.let { "https://$it/dist" }
}

object InstreamPreferences {

    fun getSharedConfig(context: Context): InstreamSharedConfig {
        val p = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        return InstreamSharedConfig(
            mdtk = p.getString("mdtk", null)?.ifEmpty { null },
            zone = p.getString("zone", null)?.ifEmpty { null },
            src = p.getString("src", null)?.ifEmpty { null },
            urlReferrer = p.getString("url_referrer", null)?.ifEmpty { null },
            tagParam = p.getString("tag_param", null)?.ifEmpty { null },
            consentStringEnabled = p.getBoolean("consent_string_enabled", true)
        )
    }

    fun saveSharedConfig(context: Context, config: InstreamSharedConfig) {
        context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE).edit()
            .putString("mdtk", config.mdtk)
            .putString("zone", config.zone)
            .putString("src", config.src)
            .putString("url_referrer", config.urlReferrer)
            .putString("tag_param", config.tagParam)
            .putBoolean("consent_string_enabled", config.consentStringEnabled)
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
            tagParam = shared.tagParam,
            consentStringEnabled = shared.consentStringEnabled,
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
            tagParam = shared.tagParam,
            consentStringEnabled = shared.consentStringEnabled,
            chromeless = p.getBoolean("chromeless", false),
            customUrl = p.getString("custom_url", null)?.ifEmpty { null },
            newplayerMode = p.getString("newplayer_mode", null)?.ifEmpty { null },
            newplayerBranchName = p.getString("newplayer_branch_name", null)?.ifEmpty { null },
            newplayerLocalIP = p.getString("newplayer_local_ip", null)?.ifEmpty { null }
        )
    }

    fun saveNoSdkConfig(
        context: Context,
        chromeless: Boolean,
        customUrl: String?,
        newplayerMode: String?,
        newplayerBranchName: String?,
        newplayerLocalIP: String?
    ) {
        context.getSharedPreferences(NOSDK_PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("chromeless", chromeless)
            .putString("custom_url", customUrl)
            .putString("newplayer_mode", newplayerMode)
            .putString("newplayer_branch_name", newplayerBranchName)
            .putString("newplayer_local_ip", newplayerLocalIP)
            .apply()
    }

    fun resetNoSdkConfig(context: Context) {
        context.getSharedPreferences(NOSDK_PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
