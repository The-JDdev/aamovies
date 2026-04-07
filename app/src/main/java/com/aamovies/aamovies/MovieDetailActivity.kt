package com.aamovies.aamovies

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.aamovies.aamovies.adapter.DownloadAdapter
import com.aamovies.aamovies.adapter.ScreenshotAdapter
import com.aamovies.aamovies.model.DownloadLink
import com.aamovies.aamovies.model.GlobalSettings
import com.aamovies.aamovies.model.Movie
import com.aamovies.aamovies.util.AdManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MovieDetailActivity : AppCompatActivity() {

    private var isReturningFromAd = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(0xFF0a0a0a.toInt())
        )

        val movieId = intent.getStringExtra("movie_id") ?: run { finish(); return }
        val movieTitle = intent.getStringExtra("movie_title") ?: "Movie"
        supportActionBar?.title = movieTitle

        val imgBanner = findViewById<ImageView>(R.id.img_banner)
        val imgPoster = findViewById<ImageView>(R.id.img_detail_poster)
        val llInfoBadges = findViewById<LinearLayout>(R.id.ll_info_badges)
        val tvTitle = findViewById<TextView>(R.id.tv_detail_title)
        val tvDescription = findViewById<TextView>(R.id.tv_detail_description)
        val tvPinnedBadge = findViewById<TextView>(R.id.tv_detail_pinned)
        val tvTrendingBadge = findViewById<TextView>(R.id.tv_detail_trending)
        val tvType = findViewById<TextView>(R.id.tv_detail_type)
        val rvScreenshots = findViewById<RecyclerView>(R.id.rv_screenshots)
        val rvDownloads = findViewById<RecyclerView>(R.id.rv_downloads)
        val sectionScreenshots = findViewById<LinearLayout>(R.id.section_screenshots)
        val sectionDownloads = findViewById<LinearLayout>(R.id.section_downloads)
        val llSocialButtons = findViewById<LinearLayout>(R.id.ll_social_buttons)
        val btnWatchlist = findViewById<TextView>(R.id.btn_watchlist)
        val btnLike = findViewById<TextView>(R.id.btn_like)
        val btnTelegram = findViewById<TextView>(R.id.btn_telegram)
        val btnFacebook = findViewById<TextView>(R.id.btn_facebook)
        val tvCastEmpty = findViewById<TextView>(R.id.tv_cast_empty)
        val hsvCast = findViewById<HorizontalScrollView>(R.id.hsv_cast)
        val llCastChips = findViewById<LinearLayout>(R.id.ll_cast_chips)

        // Load global settings for social buttons
        FirebaseDatabase.getInstance().getReference("settings/global").get()
            .addOnSuccessListener { snap ->
                val settings = snap.getValue(GlobalSettings::class.java) ?: GlobalSettings()
                val hasTelegram = settings.telegramLink.isNotEmpty()
                val hasFacebook = settings.facebookLink.isNotEmpty()
                if (hasTelegram || hasFacebook) {
                    llSocialButtons.visibility = View.VISIBLE
                    btnTelegram.visibility = if (hasTelegram) View.VISIBLE else View.GONE
                    btnFacebook.visibility = if (hasFacebook) View.VISIBLE else View.GONE
                    btnTelegram.setOnClickListener {
                        if (settings.telegramLink.isNotEmpty()) openUrl(settings.telegramLink)
                    }
                    btnFacebook.setOnClickListener {
                        if (settings.facebookLink.isNotEmpty()) openUrl(settings.facebookLink)
                    }
                }
            }

        // Load movie data
        val ref = FirebaseDatabase.getInstance().getReference("movies/$movieId")
        ref.get().addOnSuccessListener { snap ->
            val movie = snap.getValue(Movie::class.java) ?: return@addOnSuccessListener
            movie.id = movieId

            tvTitle.text = movie.title
            tvDescription.text = movie.description

            tvPinnedBadge.visibility = if (movie.pinned) View.VISIBLE else View.GONE
            tvTrendingBadge.visibility = if (movie.trending) View.VISIBLE else View.GONE

            if (movie.type.isNotEmpty()) {
                tvType.text = movie.type
                tvType.visibility = View.VISIBLE
            }

            // Poster
            if (movie.poster.isNotEmpty()) {
                Glide.with(this).load(movie.poster).centerCrop()
                    .placeholder(R.drawable.placeholder_movie).into(imgPoster)
            }

            // Banner
            val bannerUrl = movie.horizontalBanner.ifEmpty { movie.poster }
            if (bannerUrl.isNotEmpty()) {
                Glide.with(this).load(bannerUrl).centerCrop()
                    .placeholder(R.drawable.placeholder_movie).into(imgBanner)
            }

            // Info badges (Language, Genre, Industry, Rating, Quality, Year)
            addInfoBadge(llInfoBadges, movie.language, "#0d7377")
            addInfoBadge(llInfoBadges, movie.genre, "#6d28d9")
            addInfoBadge(llInfoBadges, movie.industry, "#1e3a5f")
            addInfoBadge(llInfoBadges, movie.rating, "#1a1a2e")
            addInfoBadge(llInfoBadges, movie.quality, "#003366")
            addInfoBadge(llInfoBadges, movie.year, "#333333")

            // Star cast chips
            val castNames = movie.starCast.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (castNames.isNotEmpty()) {
                hsvCast.visibility = View.VISIBLE
                castNames.forEach { name -> addCastChip(llCastChips, name) }
            } else {
                tvCastEmpty.visibility = View.VISIBLE
            }

            // Screenshots — 1 image → 1 col, 2+ images → 2-col staggered
            val screenshotUrls = movie.screenshots.values.filter { it.isNotEmpty() }
            if (screenshotUrls.isNotEmpty()) {
                sectionScreenshots.visibility = View.VISIBLE
                val spanCount = if (screenshotUrls.size == 1) 1 else 2
                rvScreenshots.layoutManager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
                rvScreenshots.adapter = ScreenshotAdapter(screenshotUrls)
            }

            // Download links
            val dlLinks = movie.downloadLinks.values.toList()
            if (dlLinks.isNotEmpty()) {
                sectionDownloads.visibility = View.VISIBLE
                rvDownloads.layoutManager = LinearLayoutManager(this)
                rvDownloads.adapter = DownloadAdapter(dlLinks) { link -> handleDownloadClick(link) }
            }

            // Increment view count
            ref.child("views").get().addOnSuccessListener { v ->
                ref.child("views").setValue((v.getValue(Long::class.java) ?: 0L) + 1L)
            }

        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load movie", Toast.LENGTH_SHORT).show()
        }

        // Watchlist & Like
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users/$uid")
            val watchRef = userRef.child("watchlist/$movieId")
            val likeRef = userRef.child("liked_movies/$movieId")

            watchRef.get().addOnSuccessListener { snap ->
                btnWatchlist.text = if (snap.exists()) "✓ In Watchlist" else "+ Watchlist"
            }
            likeRef.get().addOnSuccessListener { snap ->
                btnLike.text = if (snap.exists()) "♥ Liked" else "♡ Like"
            }

            btnWatchlist.setOnClickListener {
                watchRef.get().addOnSuccessListener { snap ->
                    if (snap.exists()) {
                        watchRef.removeValue()
                        btnWatchlist.text = "+ Watchlist"
                        Toast.makeText(this, "Removed from watchlist", Toast.LENGTH_SHORT).show()
                    } else {
                        watchRef.setValue(true)
                        btnWatchlist.text = "✓ In Watchlist"
                        Toast.makeText(this, "Added to watchlist", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            btnLike.setOnClickListener {
                likeRef.get().addOnSuccessListener { snap ->
                    if (snap.exists()) {
                        likeRef.removeValue()
                        btnLike.text = "♡ Like"
                        Toast.makeText(this, "Removed from liked movies", Toast.LENGTH_SHORT).show()
                    } else {
                        likeRef.setValue(mapOf("title" to movieTitle, "likedAt" to System.currentTimeMillis()))
                        btnLike.text = "♥ Liked"
                        Toast.makeText(this, "Added to liked movies ♥", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            btnWatchlist.visibility = View.GONE
            btnLike.visibility = View.GONE
        }
    }

    private fun addInfoBadge(container: LinearLayout, text: String, colorHex: String) {
        if (text.isEmpty()) return
        val tv = TextView(this)
        tv.text = text
        tv.textSize = 11f
        tv.setTextColor(0xFFFFFFFF.toInt())
        try { tv.setBackgroundColor(android.graphics.Color.parseColor(colorHex)) } catch (_: Exception) {}
        val dp4 = (4 * resources.displayMetrics.density).toInt()
        val dp8 = (8 * resources.displayMetrics.density).toInt()
        tv.setPadding(dp8, dp4, dp8, dp4)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = dp4
        tv.layoutParams = params
        container.addView(tv)
    }

    private fun addCastChip(container: LinearLayout, name: String) {
        val tv = TextView(this)
        tv.text = name
        tv.textSize = 12f
        tv.setTextColor(0xFFFFFFFF.toInt())
        tv.setBackgroundResource(R.drawable.bg_cast_chip)
        val dp6 = (6 * resources.displayMetrics.density).toInt()
        val dp10 = (10 * resources.displayMetrics.density).toInt()
        tv.setPadding(dp10, dp6, dp10, dp6)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.marginEnd = dp6
        tv.layoutParams = params
        container.addView(tv)
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {
            Toast.makeText(this, "Cannot open link", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isReturningFromAd) {
            isReturningFromAd = false
            val pendingUrl = AdManager.consumePendingUrl(this)
            if (!pendingUrl.isNullOrEmpty()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(pendingUrl)))
            }
        }
    }

    private fun handleDownloadClick(link: DownloadLink) {
        if (link.url.isEmpty()) {
            Toast.makeText(this, "Download link not available", Toast.LENGTH_SHORT).show()
            return
        }
        isReturningFromAd = true
        AdManager.handleDownloadLinkClick(this, link.url)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
