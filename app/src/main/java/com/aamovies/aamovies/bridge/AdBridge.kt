package com.aamovies.aamovies.bridge

import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

/**
 * AdBridge — Native double-click ad bypass.
 *
 * This logic runs entirely in native code so it cannot be blocked by browser
 * AdBlocker extensions. The HTML simply calls:
 *
 *   AndroidAd.adClick(elementId, realActionJs)
 *
 * Behaviour:
 *   First click  → opens the ad URL in the native browser (bypasses WebView adblocking)
 *   Second click within WINDOW_MS → executes realActionJs via evaluateJavascript()
 *   Timeout reset → after WINDOW_MS the counter resets; next click is again "first"
 */
class AdBridge(
    private val activity: AppCompatActivity,
    private val webView: WebView,
    private val handler: Handler
) {
    companion object {
        private const val WINDOW_MS = 3000L
        private const val AD_URL =
            "https://www.profitablecpmratenetwork.com/r03qsx6f?key=2fb4e5433393716e58400d771b255afb"
    }

    // Track last-click timestamp per element id
    private val clickMap = HashMap<String, Long>()

    /**
     * Called from HTML button onclick:
     *   AndroidAd.adClick('download_movie123', 'window.startDownload(url)')
     *
     * @param elementId     Unique ID for this clickable (e.g. "dl_movieId_linkIndex")
     * @param realActionJs  JavaScript string to eval on the second click
     */
    @JavascriptInterface
    fun adClick(elementId: String, realActionJs: String) {
        val now = System.currentTimeMillis()
        val last = clickMap[elementId] ?: 0L

        if (now - last > WINDOW_MS) {
            // ── First click: fire the ad ──────────────────────────────────────
            clickMap[elementId] = now
            handler.post {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AD_URL)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    activity.startActivity(intent)
                } catch (_: Exception) {
                    // If browser not available, skip ad and reset so next click works
                    clickMap[elementId] = 0L
                }
            }
        } else {
            // ── Second click within window: execute the real action ───────────
            clickMap[elementId] = 0L   // reset so next interaction starts fresh
            handler.post {
                webView.evaluateJavascript(realActionJs, null)
            }
        }
    }

    /**
     * Popup ad — opens a full-page ad in native browser.
     * Called from HTML to open interstitial without WebView AdBlock interference.
     */
    @JavascriptInterface
    fun showPopupAd() {
        handler.post {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AD_URL)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                activity.startActivity(intent)
            } catch (_: Exception) { }
        }
    }

    /**
     * Reset click state for a specific element (e.g. after content loads)
     */
    @JavascriptInterface
    fun resetClick(elementId: String) {
        clickMap[elementId] = 0L
    }
}
