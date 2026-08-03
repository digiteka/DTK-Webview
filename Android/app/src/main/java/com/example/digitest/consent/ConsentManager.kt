package com.example.digitest.consent

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object ConsentManager {

    private const val TAG = "ConsentManager"

    // TODO: Remplir avec votre hash device visible dans logcat au premier lancement :
    // Cherchez : "Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("XXXXXXXX")"
    private const val TEST_DEVICE_HASH_ID = ""

    fun requestConsent(activity: Activity, onConsentGathered: () -> Unit) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        val debugSettingsBuilder = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)

        if (TEST_DEVICE_HASH_ID.isNotEmpty()) {
            debugSettingsBuilder.addTestDeviceHashedId(TEST_DEVICE_HASH_ID)
        } else {
            Log.w(TAG, "TEST_DEVICE_HASH_ID non renseigné — cherchez dans logcat : " +
                    "\"Use new ConsentDebugSettings.Builder().addTestDeviceHashedId(...)\"")
        }

        val params = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettingsBuilder.build())
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                Log.d(TAG, "Consent status: ${consentInformation.consentStatus}")
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Consent form error: ${formError.message}")
                    }
                    onConsentGathered()
                }
            },
            { requestConsentError ->
                Log.w(TAG, "Consent info update failed: ${requestConsentError.message}")
                onConsentGathered()
            }
        )
    }

    fun getTcString(context: Context): String? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString("IABTCF_TCString", null)
    }
}
