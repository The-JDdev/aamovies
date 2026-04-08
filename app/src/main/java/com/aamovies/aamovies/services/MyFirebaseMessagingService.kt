package com.aamovies.aamovies.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aamovies.aamovies.MainActivity
import com.aamovies.aamovies.R
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.HttpURLConnection
import java.net.URL

/**
 * MyFirebaseMessagingService — Native FCM receiver.
 *
 * Responsibilities:
 *  1. Silent topic subscription on first launch (no user interaction required)
 *  2. Display rich push notifications (title, body, image if provided)
 *  3. Handle data-only messages (silent push)
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        const val TOPIC = "new_movies"
        private const val CHANNEL_ID = "aamovies_notifications"
        private const val CHANNEL_NAME = "AAMovies Notifications"
        private const val PREFS_NAME = "aamovies_prefs"
        private const val KEY_SUBSCRIBED = "fcm_subscribed_v2"

        /**
         * Called from MainActivity.onCreate() — subscribes silently on very first launch.
         * Uses SharedPreferences to ensure this runs only once.
         */
        fun subscribeIfNeeded(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            if (!prefs.getBoolean(KEY_SUBSCRIBED, false)) {
                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
                    .addOnSuccessListener {
                        prefs.edit().putBoolean(KEY_SUBSCRIBED, true).apply()
                        Log.d(TAG, "Silently subscribed to topic: $TOPIC")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Topic subscription failed: ${e.message}")
                        // Will retry on next launch since we don't save the flag
                    }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed: $token")
        // Re-subscribe when token refreshes
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM received from: ${message.from}")

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "New Movie on AAMovies"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: return

        val imageUrl = message.notification?.imageUrl?.toString()
            ?: message.data["image"]

        val posterBitmap: Bitmap? = imageUrl?.let { downloadBitmap(it) }

        showNotification(title, body, posterBitmap)
    }

    private fun downloadBitmap(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.doInput = true
            connection.connect()
            BitmapFactory.decodeStream(connection.inputStream)
        } catch (e: Exception) {
            Log.e(TAG, "Poster download failed: ${e.message}")
            null
        }
    }

    private fun showNotification(title: String, body: String, posterBitmap: Bitmap?) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New movie alerts from AAMovies"
                enableLights(true)
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        else PendingIntent.FLAG_UPDATE_CURRENT

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingFlags)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val appIcon = BitmapFactory.decodeResource(resources, R.drawable.logo)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(posterBitmap ?: appIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        if (posterBitmap != null) {
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(posterBitmap)
                    .bigLargeIcon(null as Bitmap?)
                    .setBigContentTitle(title)
                    .setSummaryText(body)
            )
        } else {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
        }

        nm.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
