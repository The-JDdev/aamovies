package com.aamovies.aamovies.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

object AdManager {

    private const val AD_REDIRECT_URL =
        "https://www.profitablecpmratenetwork.com/r03qsx6f?key=2fb4e5433393716e58400d771b255afb"
    private const val AD_POPUP_SCRIPT_URL =
        "https://pl25504968.profitablecpmratenetwork.com/e2/5b/7a/e25b7a7d319c7998d0efd502126b96d1.js"

    const val PREF_PENDING_URL = "pending_target_url"

    private var backgroundAdWebView: WebView? = null

    fun initBackgroundAdWebView(context: Context) {
        backgroundAdWebView = WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                userAgentString = "Mozilla/5.0 (Linux; Android 11; Mobile) AppleWebKit/537.36"
            }
            webViewClient = WebViewClient()
            loadUrl("about:blank")
            postDelayed({
                evaluateJavascript(
                    """
                    (function(){
                        var s=document.createElement('script');
                        s.src='$AD_POPUP_SCRIPT_URL';
                        s.async=true;
                        document.head.appendChild(s);
                    })();
                    """.trimIndent(), null
                )
            }, 3000L)
        }
    }

    fun handleMovieLinkClick(context: Context, targetUrl: String) {
        val prefs = context.getSharedPreferences("ad_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_PENDING_URL, targetUrl).apply()
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AD_REDIRECT_URL))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun consumePendingUrl(context: Context): String? {
        val prefs = context.getSharedPreferences("ad_prefs", Context.MODE_PRIVATE)
        val url = prefs.getString(PREF_PENDING_URL, null)
        if (url != null) prefs.edit().remove(PREF_PENDING_URL).apply()
        return url
    }

    fun destroy() {
        backgroundAdWebView?.destroy()
        backgroundAdWebView = null
    }
}
