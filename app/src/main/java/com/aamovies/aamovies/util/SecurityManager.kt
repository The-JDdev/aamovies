package com.aamovies.aamovies.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.aamovies.aamovies.BuildConfig
import java.io.File
import java.security.MessageDigest

object SecurityManager {

    private const val TAG = "SecurityManager"

    // SHA-256 fingerprint of the original release signing certificate.
    // Compute via: keytool -list -v -keystore aamovies.keystore -alias aamovies
    // Replace with actual fingerprint after first production build.
    private const val EXPECTED_CERT_SHA256 = "BUILD_TIME_FILL_IN"

    fun runChecks(context: Context) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Security checks running in DEBUG mode — enforcement disabled.")
            return
        }
        val rooted = isRooted()
        val emulated = isEmulator()
        val sigValid = isSignatureValid(context)

        if (rooted) Log.w(TAG, "Root detected")
        if (emulated) Log.w(TAG, "Emulator detected")
        if (!sigValid) Log.e(TAG, "Signature mismatch — possible APK tampering!")

        // In release builds, tampered signature = block app
        if (!sigValid) {
            throw SecurityException("APP_TAMPERED")
        }
    }

    fun isRooted(): Boolean {
        val rootPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            "/system/bin/.ext/.su",
            "/system/usr/we-need-root/su-backup"
        )
        if (rootPaths.any { File(it).exists() }) return true
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            p.inputStream.read() != -1
        } catch (e: Exception) { false }
    }

    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu"))
    }

    fun isSignatureValid(context: Context): Boolean {
        // Skip check if placeholder is still present (dev environment)
        if (EXPECTED_CERT_SHA256 == "BUILD_TIME_FILL_IN") return true
        return try {
            val pm = context.packageManager
            val sigs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                    .signingInfo.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures
            }
            val digest = MessageDigest.getInstance("SHA-256")
            val actual = sigs.first().toByteArray()
            val hash = digest.digest(actual).joinToString("") { "%02x".format(it) }
            hash.equals(EXPECTED_CERT_SHA256, ignoreCase = true)
        } catch (e: Exception) {
            Log.e(TAG, "Signature check error: ${e.message}")
            false
        }
    }
}
