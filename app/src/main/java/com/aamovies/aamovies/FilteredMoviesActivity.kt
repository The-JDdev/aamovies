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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FilteredMoviesActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FILTER_TYPE = "filter_type"
        const val EXTRA_FILTER_VALUE = "filter_value"
        const val TYPE_CATEGORY = "category"
        const val TYPE_GENRE = "genre"
        const val TYPE_YEAR = "year"
    }

    private var pendingMovieId: String? = null
    private var pendingMovieTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filtered_movies)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(0xFF0a0a0a.toInt())
        )

        val filterType = intent.getStringExtra(EXTRA_FILTER_TYPE) ?: TYPE_CATEGORY
        val filterValue = intent.getStringExtra(EXTRA_FILTER_VALUE) ?: return

        val label = when (filterType) {
            TYPE_CATEGORY -> "Category: $filterValue"
            TYPE_GENRE -> "Genre: $filterValue"
            TYPE_YEAR -> "Year: $filterValue"
            else -> filterValue
        }
        supportActionBar?.title = label

        val rvMovies = findViewById<RecyclerView>(R.id.rv_filtered_movies)
        val tvEmpty = findViewById<TextView>(R.id.tv_filtered_empty)
        val tvCount = findViewById<TextView>(R.id.tv_filtered_count)

        rvMovies.layoutManager = GridLayoutManager(this, 2)

        FirebaseDatabase.getInstance().getReference("movies")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val filtered = mutableListOf<Movie>()
                    snapshot.children.forEach { child ->
                        val movie = child.getValue(Movie::class.java) ?: return@forEach
                        movie.id = child.key ?: return@forEach
                        val matches = when (filterType) {
                            TYPE_CATEGORY -> movie.category.equals(filterValue, ignoreCase = true)
                            TYPE_GENRE -> movie.genre.contains(filterValue, ignoreCase = true)
                            TYPE_YEAR -> movie.year == filterValue
                            else -> false
                        }
                        if (matches) filtered.add(movie)
                    }
                    // Pinned first
                    val sorted = filtered.sortedWith(
                        compareByDescending<Movie> { it.pinned }
                            .thenByDescending { it.trending }
                            .thenByDescending { it.createdAt }
                    )
                    val adapter = MovieAdapter(sorted) { movie ->
                        handleCardClick(movie)
                    }
                    rvMovies.adapter = adapter
                    tvCount.text = "${sorted.size} result${if (sorted.size != 1) "s" else ""}"
                    tvEmpty.visibility = if (sorted.isEmpty()) View.VISIBLE else View.GONE
                    tvCount.visibility = if (sorted.isNotEmpty()) View.VISIBLE else View.GONE
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onResume() {
        super.onResume()
        val pending = AdManager.consumePendingMovieId(this)
        if (pending != null) {
            val (movieId, movieTitle) = pending
            openDetail(movieId, movieTitle)
        }
    }

    private fun handleCardClick(movie: Movie) {
        AdManager.handleMovieCardClick(this, movie.id, movie.title)
    }

    private fun openDetail(movieId: String, movieTitle: String) {
        startActivity(Intent(this, MovieDetailActivity::class.java).apply {
            putExtra("movie_id", movieId)
            putExtra("movie_title", movieTitle)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { onBackPressedDispatcher.onBackPressed(); return true }
        return super.onOptionsItemSelected(item)
    }
}
