package com.aamovies.aamovies.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aamovies.aamovies.FilteredMoviesActivity
import com.aamovies.aamovies.LikedMoviesActivity
import com.aamovies.aamovies.LoginActivity
import com.aamovies.aamovies.MovieDetailActivity
import com.aamovies.aamovies.R
import com.aamovies.aamovies.adapter.ChipAdapter
import com.aamovies.aamovies.adapter.MovieAdapter
import com.aamovies.aamovies.model.Movie
import com.aamovies.aamovies.util.AdManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var rvTrending: RecyclerView
    private lateinit var rvFeatured: RecyclerView
    private lateinit var rvCategories: RecyclerView
    private lateinit var rvGenres: RecyclerView
    private lateinit var rvYears: RecyclerView
    private lateinit var tvEmptyTrending: TextView
    private lateinit var tvEmptyFeatured: TextView
    private var trendingAdapter: MovieAdapter? = null
    private var featuredAdapter: MovieAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvTrending = view.findViewById(R.id.rv_trending)
        rvFeatured = view.findViewById(R.id.rv_featured)
        rvCategories = view.findViewById(R.id.rv_filter_categories)
        rvGenres = view.findViewById(R.id.rv_filter_genres)
        rvYears = view.findViewById(R.id.rv_filter_years)
        tvEmptyTrending = view.findViewById(R.id.tv_empty_trending)
        tvEmptyFeatured = view.findViewById(R.id.tv_empty_featured)
        val btnOverflow = view.findViewById<ImageView>(R.id.btn_overflow_menu)

        rvTrending.layoutManager = GridLayoutManager(requireContext(), 2)
        rvFeatured.layoutManager = GridLayoutManager(requireContext(), 2)
        rvCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvGenres.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvYears.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        trendingAdapter = MovieAdapter(emptyList()) { movie -> handleMovieCardClick(movie) }
        featuredAdapter = MovieAdapter(emptyList()) { movie -> handleMovieCardClick(movie) }
        rvTrending.adapter = trendingAdapter
        rvFeatured.adapter = featuredAdapter

        btnOverflow.setOnClickListener { v -> showOverflowMenu(v) }

        loadMovies()
        loadFilters()
    }

    override fun onResume() {
        super.onResume()
        val pending = AdManager.consumePendingMovieId(requireContext())
        if (pending != null) {
            val (movieId, movieTitle) = pending
            openDetail(movieId, movieTitle)
        }
    }

    private fun handleMovieCardClick(movie: Movie) {
        AdManager.handleMovieCardClick(requireContext(), movie.id, movie.title)
    }

    private fun openDetail(movieId: String, movieTitle: String) {
        startActivity(Intent(requireContext(), MovieDetailActivity::class.java).apply {
            putExtra("movie_id", movieId)
            putExtra("movie_title", movieTitle)
        })
    }

    private fun openFilter(filterType: String, value: String) {
        startActivity(Intent(requireContext(), FilteredMoviesActivity::class.java).apply {
            putExtra(FilteredMoviesActivity.EXTRA_FILTER_TYPE, filterType)
            putExtra(FilteredMoviesActivity.EXTRA_FILTER_VALUE, value)
        })
    }

    private fun loadMovies() {
        FirebaseDatabase.getInstance().getReference("movies")
            .orderByChild("createdAt")
            .limitToLast(50)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val all = mutableListOf<Movie>()
                    snapshot.children.forEach { child ->
                        val movie = child.getValue(Movie::class.java) ?: return@forEach
                        movie.id = child.key ?: return@forEach
                        all.add(movie)
                    }
                    val sorted = all.sortedWith(
                        compareByDescending<Movie> { it.pinned }
                            .thenByDescending { it.trending }
                            .thenByDescending { it.createdAt }
                    )
                    val trendingList = sorted.filter { it.trending || it.pinned }
                    val featuredList = sorted.take(20)
                    trendingAdapter?.updateMovies(trendingList)
                    featuredAdapter?.updateMovies(featuredList)
                    tvEmptyTrending.visibility = if (trendingList.isEmpty()) View.VISIBLE else View.GONE
                    tvEmptyFeatured.visibility = if (featuredList.isEmpty()) View.VISIBLE else View.GONE
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadFilters() {
        if (!isAdded) return

        // Load categories from /categories node
        FirebaseDatabase.getInstance().getReference("categories")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val cats = mutableListOf<String>()
                    snapshot.children.forEach { child ->
                        val name = child.child("name").getValue(String::class.java)
                        if (!name.isNullOrEmpty()) cats.add(name)
                    }
                    if (cats.isNotEmpty()) {
                        rvCategories.adapter = ChipAdapter(cats) { cat ->
                            openFilter(FilteredMoviesActivity.TYPE_CATEGORY, cat)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // Load genres and years from movies
        FirebaseDatabase.getInstance().getReference("movies")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val genres = mutableSetOf<String>()
                    val years = mutableSetOf<String>()
                    snapshot.children.forEach { child ->
                        val genre = child.child("genre").getValue(String::class.java) ?: ""
                        val year = child.child("year").getValue(String::class.java) ?: ""
                        if (genre.isNotEmpty()) {
                            genre.split(",").forEach { g ->
                                val trimmed = g.trim()
                                if (trimmed.isNotEmpty()) genres.add(trimmed)
                            }
                        }
                        if (year.isNotEmpty()) years.add(year)
                    }
                    val sortedGenres = genres.sorted()
                    val sortedYears = years.sortedDescending()

                    if (sortedGenres.isNotEmpty()) {
                        rvGenres.adapter = ChipAdapter(sortedGenres) { genre ->
                            openFilter(FilteredMoviesActivity.TYPE_GENRE, genre)
                        }
                    }
                    if (sortedYears.isNotEmpty()) {
                        rvYears.adapter = ChipAdapter(sortedYears) { year ->
                            openFilter(FilteredMoviesActivity.TYPE_YEAR, year)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showOverflowMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.home_overflow_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_liked -> {
                    startActivity(Intent(requireContext(), LikedMoviesActivity::class.java))
                    true
                }
                R.id.menu_contact -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("jdvijay.me@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "Support: AAMovies 2.0")
                    }
                    if (intent.resolveActivity(requireContext().packageManager) != null) {
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.menu_profile -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.menu_signout -> {
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(requireContext(), "Signed out", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}
