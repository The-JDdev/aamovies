package com.aamovies.aamovies.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * AdManager handles ad redirect logic and the optional invisible background popup WebView.
 *
 * TO CONFIGURE:
 *   1. Fill in AD_REDIRECT_URL with your redirect/interstitial ad link.
 *   2. Fill in AD_POPUP_SCRIPT_URL with your popup ad script URL.
 *   3. Both values are intentionally left empty in this public repository.
 */
object AdManager {

    // TODO: Set your redirect/interstitial ad URL here
    private const val AD_REDIRECT_URL = ""

    // TODO: Set your popup ad script URL here (loaded in an invisible background WebView)
    private const val AD_POPUP_SCRIPT_URL = ""

    private const val AD_PREFS = "ad_prefs"
    const val PREF_PENDING_DOWNLOAD_URL = "pending_target_url"
    const val PREF_PENDING_MOVIE_ID = "pending_movie_id"
    const val PREF_PENDING_MOVIE_TITLE = "pending_movie_title"

    private var backgroundAdWebView: WebView? = null

    /** Initializes an invisible WebView that loads the popup ad script after 3 seconds. */
    fun initBackgroundAdWebView(context: Context) {
        if (AD_POPUP_SCRIPT_URL.isEmpty()) return
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

    /** Card click: stores pending movie ID → fires ad → on Back → HomeFragment opens detail. */
    fun handleMovieCardClick(context: Context, movieId: String, movieTitle: String) {
        val prefs = context.getSharedPreferences(AD_PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(PREF_PENDING_MOVIE_ID, movieId)
            .putString(PREF_PENDING_MOVIE_TITLE, movieTitle)
            .apply()
        if (AD_REDIRECT_URL.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AD_REDIRECT_URL))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            // No ad URL set — open detail directly
            consumePendingMovieId(context) // clear prefs
            val intent = Intent(context, Class.forName("com.aamovies.aamovies.MovieDetailActivity"))
            intent.putExtra("movie_id", movieId)
            intent.putExtra("movie_title", movieTitle)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /** Download click: stores pending URL → fires ad → on Back → MovieDetailActivity opens URL. */
    fun handleDownloadLinkClick(context: Context, downloadUrl: String) {
        val prefs = context.getSharedPreferences(AD_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_PENDING_DOWNLOAD_URL, downloadUrl).apply()
        if (AD_REDIRECT_URL.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AD_REDIRECT_URL))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun handleMovieLinkClick(context: Context, targetUrl: String) {
        handleDownloadLinkClick(context, targetUrl)
    }

    fun consumePendingMovieId(context: Context): Pair<String, String>? {
        val prefs = context.getSharedPreferences(AD_PREFS, Context.MODE_PRIVATE)
        val id = prefs.getString(PREF_PENDING_MOVIE_ID, null) ?: return null
        val title = prefs.getString(PREF_PENDING_MOVIE_TITLE, "") ?: ""
        prefs.edit().remove(PREF_PENDING_MOVIE_ID).remove(PREF_PENDING_MOVIE_TITLE).apply()
        return Pair(id, title)
    }

    fun consumePendingUrl(context: Context): String? {
        val prefs = context.getSharedPreferences(AD_PREFS, Context.MODE_PRIVATE)
        val url = prefs.getString(PREF_PENDING_DOWNLOAD_URL, null)
        if (url != null) prefs.edit().remove(PREF_PENDING_DOWNLOAD_URL).apply()
        return url
    }

    fun destroy() {
        backgroundAdWebView?.destroy()
        backgroundAdWebView = null
    }
}
