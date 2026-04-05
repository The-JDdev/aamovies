package com.aamovies.aamovies.bridge

import android.os.Handler
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging

/**
 * FCMBridge — Native FCM token retrieval.
 * Topic subscription happens automatically on launch via MyFirebaseMessagingService.
 * HTML can request the current FCM token via AndroidFCM.getToken()
 */
class FCMBridge(
    private val activity: AppCompatActivity,
    private val webView: WebView,
    private val handler: Handler
) {
    @JavascriptInterface
    fun getToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                js("window.onFCMToken('$token')")
            }
            .addOnFailureListener { e ->
                js("window.onFCMTokenError('${e.message}')")
            }
    }

    @JavascriptInterface
    fun subscribeTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnSuccessListener { js("window.onFCMSubscribed('$topic')") }
            .addOnFailureListener { e -> js("window.onFCMError('${e.message}')") }
    }

    @JavascriptInterface
    fun unsubscribeTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnSuccessListener { js("window.onFCMUnsubscribed('$topic')") }
            .addOnFailureListener { e -> js("window.onFCMError('${e.message}')") }
    }

    private fun js(script: String) {
        handler.post { webView.evaluateJavascript(script, null) }
    }
}
