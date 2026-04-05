package com.aamovies.aamovies

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aamovies.aamovies.adapter.DownloadAdapter
import com.aamovies.aamovies.adapter.ScreenshotAdapter
import com.aamovies.aamovies.model.DownloadLink
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

        val imgPoster = findViewById<ImageView>(R.id.img_detail_poster)
        val tvTitle = findViewById<TextView>(R.id.tv_detail_title)
        val tvYear = findViewById<TextView>(R.id.tv_detail_year)
        val tvCategory = findViewById<TextView>(R.id.tv_detail_category)
        val tvLanguage = findViewById<TextView>(R.id.tv_detail_language)
        val tvQuality = findViewById<TextView>(R.id.tv_detail_quality)
        val tvDescription = findViewById<TextView>(R.id.tv_detail_description)
        val rvScreenshots = findViewById<RecyclerView>(R.id.rv_screenshots)
        val rvDownloads = findViewById<RecyclerView>(R.id.rv_downloads)
        val sectionScreenshots = findViewById<LinearLayout>(R.id.section_screenshots)
        val sectionDownloads = findViewById<LinearLayout>(R.id.section_downloads)
        val btnWatchlist = findViewById<TextView>(R.id.btn_watchlist)
        val btnLike = findViewById<TextView>(R.id.btn_like)

        val ref = FirebaseDatabase.getInstance().getReference("movies/$movieId")
        ref.get().addOnSuccessListener { snap ->
            val movie = snap.getValue(Movie::class.java) ?: return@addOnSuccessListener
            movie.id = movieId

            tvTitle.text = movie.title
            tvYear.text = movie.year
            tvCategory.text = movie.category
            tvLanguage.text = movie.language.ifEmpty { "—" }
            tvQuality.text = movie.quality.ifEmpty { "HD" }
            tvDescription.text = movie.description

            if (movie.poster.isNotEmpty()) {
                Glide.with(this).load(movie.poster).centerCrop()
                    .placeholder(R.drawable.placeholder_movie).into(imgPoster)
            }

            val dlLinks = movie.downloadLinks.values.toList()
            if (dlLinks.isNotEmpty()) {
                sectionDownloads.visibility = View.VISIBLE
                rvDownloads.layoutManager = LinearLayoutManager(this)
                rvDownloads.adapter = DownloadAdapter(dlLinks) { link ->
                    handleDownloadClick(link)
                }
            }

            sectionScreenshots.visibility = View.GONE

            ref.child("views").get().addOnSuccessListener { v ->
                ref.child("views").setValue((v.getValue(Long::class.java) ?: 0L) + 1L)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load movie", Toast.LENGTH_SHORT).show()
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users/$uid")
            val watchRef = userRef.child("watchlist/$movieId")
            val likeRef = userRef.child("liked/$movieId")

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
                    } else {
                        watchRef.setValue(true)
                        btnWatchlist.text = "✓ In Watchlist"
                    }
                }
            }
            btnLike.setOnClickListener {
                likeRef.get().addOnSuccessListener { snap ->
                    if (snap.exists()) {
                        likeRef.removeValue()
                        btnLike.text = "♡ Like"
                    } else {
                        likeRef.setValue(true)
                        btnLike.text = "♥ Liked"
                    }
                }
            }
        } else {
            btnWatchlist.visibility = View.GONE
            btnLike.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        if (isReturningFromAd) {
            isReturningFromAd = false
            val pendingUrl = AdManager.consumePendingUrl(this)
            if (!pendingUrl.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pendingUrl))
                startActivity(intent)
            }
        }
    }

    private fun handleDownloadClick(link: DownloadLink) {
        if (link.url.isEmpty()) {
            Toast.makeText(this, "Download link not available", Toast.LENGTH_SHORT).show()
            return
        }
        isReturningFromAd = true
        AdManager.handleMovieLinkClick(this, link.url)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
