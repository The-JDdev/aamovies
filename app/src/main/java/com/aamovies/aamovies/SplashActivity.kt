package com.aamovies.aamovies

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aamovies.aamovies.services.MyFirebaseMessagingService

/**
 * SplashActivity — Native Netflix-style splash screen.
 *
 * Animation timeline:
 *  0ms      Logo at 0.3x, 0% alpha. Overlay fully black.
 *  0-700ms  Overlay fades out. Logo zooms in (0.3→1.0x) and fades in (0→100%).
 *  700-900ms Hold.
 *  900-1300ms Logo zooms forward slightly (1.0→1.2x) and fades out.
 *  1350ms   Navigate to MainActivity with crossfade.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.splash_logo)
        val overlay = findViewById<View>(R.id.splash_overlay)
        val tagline = findViewById<TextView>(R.id.splash_tagline)

        // Initial state
        logo.alpha = 0f
        overlay.alpha = 1f
        tagline.alpha = 0f

        // ── Phase 1: 0–700ms  ── Zoom-in + Fade-in ───────────────────────────
        val overlayFadeOut = AlphaAnimation(1f, 0f).apply {
            duration = 700
            interpolator = AccelerateDecelerateInterpolator()
            fillAfter = true
        }

        val logoZoomIn = ScaleAnimation(
            0.3f, 1.0f, 0.3f, 1.0f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 700
            interpolator = AccelerateDecelerateInterpolator()
            fillAfter = true
        }

        val logoFadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
            fillAfter = true
        }

        val taglineFadeIn = AlphaAnimation(0f, 0.7f).apply {
            startOffset = 400
            duration = 400
            fillAfter = true
        }

        // ── Phase 2: 900–1300ms ── Zoom forward + Fade-out ────────────────────
        val logoZoomOut = ScaleAnimation(
            1.0f, 1.25f, 1.0f, 1.25f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            startOffset = 900
            duration = 400
            interpolator = AccelerateInterpolator()
            fillAfter = true
        }

        val logoFadeOut = AlphaAnimation(1f, 0f).apply {
            startOffset = 900
            duration = 350
            interpolator = AccelerateInterpolator()
            fillAfter = true
        }

        val overlayFadeIn = AlphaAnimation(0f, 1f).apply {
            startOffset = 950
            duration = 350
            interpolator = AccelerateInterpolator()
            fillAfter = true
        }

        // Apply animations
        overlay.startAnimation(overlayFadeOut)
        overlay.postDelayed({ overlay.startAnimation(overlayFadeIn) }, 0)

        val logoIntro = AnimationSet(false).apply {
            addAnimation(logoZoomIn)
            addAnimation(logoFadeIn)
            fillAfter = true
        }
        val logoOutro = AnimationSet(false).apply {
            addAnimation(logoZoomOut)
            addAnimation(logoFadeOut)
            fillAfter = true
        }

        logo.startAnimation(logoIntro)
        logo.postDelayed({ logo.startAnimation(logoOutro) }, 0)
        tagline.startAnimation(taglineFadeIn)

        // Subscribe to FCM topic silently on first launch
        MyFirebaseMessagingService.subscribeIfNeeded(this)

        // Navigate to MainActivity after animation completes
        logo.postDelayed({ launch() }, 1350)
    }

    private fun launch() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
