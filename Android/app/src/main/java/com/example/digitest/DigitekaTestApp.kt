package com.example.digitest

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import android.webkit.CookieManager
import android.webkit.WebView
import com.digiteka.instream.InStream
import com.digiteka.instream.core.config.DTKISConfig
import com.example.digitest.DEFAULT_IS_MDTK
import com.example.digitest.InstreamPreferences
import com.example.digitest.utils.enableThirdPartyCookiesRecursively

class DigitekaTestApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        CookieManager.getInstance().setAcceptCookie(true)

        registerActivityLifecycleCallbacks(ThirdPartyCookiesLifecycleCallbacks())

        try {
            val mdtk = InstreamPreferences.getSdkConfig(applicationContext).mdtk ?: DEFAULT_IS_MDTK
            val dtkisConfig = DTKISConfig.Builder(mdtk = mdtk).build()
            InStream.initialize(applicationContext = applicationContext, config = dtkisConfig)
            Log.d("DigitekaTestApp", "InStream SDK initialized")
        } catch (e: Exception) {
            Log.e("DigitekaTestApp", "InStream SDK init failed: ${e.message}")
        }
    }

    /**
     * Fallback global : active les cookies tiers sur toutes les WebViews présentes
     * dans chaque Activity (couvre aussi VideoFeedActivity lancé en plein écran).
     * Un OnGlobalLayoutListener est attaché à decorView pour attraper les WebViews
     * créées après le resume (initialisation asynchrone des SDK).
     */
    private class ThirdPartyCookiesLifecycleCallbacks : ActivityLifecycleCallbacks {

        // Associe chaque Activity à son listener pour éviter les doublons et pouvoir le retirer
        private val listeners = HashMap<Activity, ViewTreeObserver.OnGlobalLayoutListener>()

        override fun onActivityResumed(activity: Activity) {
            val decorView = activity.window?.decorView ?: return

            // Scan immédiat des WebViews déjà présentes
            enableThirdPartyCookiesRecursively(decorView)

            // Listener persistant pour les WebViews créées après le resume
            if (!listeners.containsKey(activity)) {
                val listener = ViewTreeObserver.OnGlobalLayoutListener {
                    enableThirdPartyCookiesRecursively(decorView)
                }
                decorView.viewTreeObserver.addOnGlobalLayoutListener(listener)
                listeners[activity] = listener
            }
        }

        override fun onActivityPaused(activity: Activity) {
            val decorView = activity.window?.decorView ?: return
            listeners.remove(activity)?.let {
                decorView.viewTreeObserver.removeOnGlobalLayoutListener(it)
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) { listeners.remove(activity) }
    }
}
