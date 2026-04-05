package com.aamovies.aamovies.bridge

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * DownloadBridge — Native Android DownloadManager integration.
 *
 * HTML calls: AndroidDownload.download(url, filename, title)
 * This uses the system DownloadManager so downloads survive app close,
 * appear in the notification bar, and save to the device Downloads folder.
 */
class DownloadBridge(
    private val activity: AppCompatActivity,
    private val webView: WebView,
    private val handler: Handler
) {
    @JavascriptInterface
    fun download(url: String, filename: String, title: String) {
        handler.post {
            try {
                val sanitized = filename.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
                val safeTitle = if (title.isBlank()) sanitized else title

                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setTitle(safeTitle)
                    setDescription("Downloading via Aamovies")
                    setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                    )
                    setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, sanitized
                    )
                    addRequestHeader("User-Agent", "Mozilla/5.0 (Android)")
                    setAllowedOverMetered(true)
                    setAllowedOverRoaming(true)
                }

                val dm = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val downloadId = dm.enqueue(request)

                Toast.makeText(activity, "Download started: $safeTitle", Toast.LENGTH_SHORT).show()
                webView.evaluateJavascript(
                    "window.onNativeDownloadStarted('${downloadId}', '${sanitized}')", null
                )
            } catch (e: Exception) {
                Toast.makeText(activity, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                webView.evaluateJavascript(
                    "window.onNativeDownloadError('${e.message}')", null
                )
            }
        }
    }

    /**
     * Open a URL directly in the native browser — used for external streaming links
     * or download mirrors that must bypass the WebView.
     */
    @JavascriptInterface
    fun openInBrowser(url: String) {
        handler.post {
            try {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW, Uri.parse(url)
                ).apply { flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK }
                activity.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(activity, "Cannot open link", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
