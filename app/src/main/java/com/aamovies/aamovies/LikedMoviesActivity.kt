package com.aamovies.aamovies

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aamovies.aamovies.adapter.MovieAdapter
import com.aamovies.aamovies.model.Movie
import com.aamovies.aamovies.util.AdManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LikedMoviesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liked_movies)

        supportActionBar?.title = "♥ Liked Movies"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(0xFF0a0a0a.toInt())
        )

        val rvMovies = findViewById<RecyclerView>(R.id.rv_liked_movies)
        val tvEmpty = findViewById<TextView>(R.id.tv_liked_empty)
        val tvCount = findViewById<TextView>(R.id.tv_liked_count)

        rvMovies.layoutManager = GridLayoutManager(this, 2)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            tvEmpty.text = "Sign in to see your liked movies."
            tvEmpty.visibility = View.VISIBLE
            return
        }

        FirebaseDatabase.getInstance().getReference("users/$uid/liked_movies")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists() || !snapshot.hasChildren()) {
                        tvEmpty.visibility = View.VISIBLE
                        tvCount.visibility = View.GONE
                        rvMovies.adapter = null
                        return
                    }
                    val movieIds = snapshot.children.mapNotNull { it.key }.toList()
                    tvCount.text = "Loading ${movieIds.size} liked movie${if (movieIds.size != 1) "s" else ""}..."
                    tvCount.visibility = View.VISIBLE
                    tvEmpty.visibility = View.GONE

                    val likedMovies = mutableListOf<Movie>()
                    var loadCount = 0
                    val db = FirebaseDatabase.getInstance().getReference("movies")

                    for (movieId in movieIds) {
                        db.child(movieId).get().addOnSuccessListener { snap ->
                            val movie = snap.getValue(Movie::class.java)
                            if (movie != null) {
                                movie.id = movieId
                                likedMovies.add(movie)
                            }
                            loadCount++
                            if (loadCount == movieIds.size) {
                                val sorted = likedMovies.sortedByDescending { it.createdAt }
                                rvMovies.adapter = MovieAdapter(sorted) { m ->
                                    AdManager.handleMovieCardClick(this@LikedMoviesActivity, m.id, m.title)
                                }
                                tvCount.text = "${sorted.size} liked movie${if (sorted.size != 1) "s" else ""}"
                                tvEmpty.visibility = if (sorted.isEmpty()) View.VISIBLE else View.GONE
                            }
                        }.addOnFailureListener {
                            loadCount++
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onResume() {
        super.onResume()
        val pending = AdManager.consumePendingMovieId(this)
        if (pending != null) {
            startActivity(Intent(this, MovieDetailActivity::class.java).apply {
                putExtra("movie_id", pending.first)
                putExtra("movie_title", pending.second)
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { onBackPressedDispatcher.onBackPressed(); return true }
        return super.onOptionsItemSelected(item)
    }
}
