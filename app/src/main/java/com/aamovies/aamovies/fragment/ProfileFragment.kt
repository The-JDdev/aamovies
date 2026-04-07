package com.aamovies.aamovies.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aamovies.aamovies.LoginActivity
import com.aamovies.aamovies.MovieDetailActivity
import com.aamovies.aamovies.R
import com.aamovies.aamovies.adapter.MovieAdapter
import com.aamovies.aamovies.model.Movie
import com.aamovies.aamovies.util.AdManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private var likedAdapter: MovieAdapter? = null
    private var watchlistAdapter: MovieAdapter? = null
    private var allMoviesCache: Map<String, Movie> = emptyMap()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName = view.findViewById<TextView>(R.id.tv_profile_name)
        val tvEmail = view.findViewById<TextView>(R.id.tv_profile_email)
        val btnLogout = view.findViewById<Button>(R.id.btn_logout)
        val rvLiked = view.findViewById<RecyclerView>(R.id.rv_liked)
        val rvWatchlist = view.findViewById<RecyclerView>(R.id.rv_watchlist)
        val tvLikedEmpty = view.findViewById<TextView>(R.id.tv_liked_empty)
        val tvWatchlistEmpty = view.findViewById<TextView>(R.id.tv_watchlist_empty)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            tvName.text = if (user.isAnonymous) "Guest User" else (user.displayName ?: "User")
            tvEmail.text = if (user.isAnonymous) "Anonymous" else (user.email ?: "—")
        }

        // Liked grid (no tags, no meta)
        rvLiked.layoutManager = GridLayoutManager(requireContext(), 2)
        likedAdapter = MovieAdapter(emptyList(), isHorizontal = false, showTags = false) { movie ->
            AdManager.handleMovieCardClick(requireContext(), movie.id, movie.title)
        }
        rvLiked.adapter = likedAdapter
        rvLiked.isNestedScrollingEnabled = false

        // Watchlist grid (no tags, no meta)
        rvWatchlist.layoutManager = GridLayoutManager(requireContext(), 2)
        watchlistAdapter = MovieAdapter(emptyList(), isHorizontal = false, showTags = false) { movie ->
            AdManager.handleMovieCardClick(requireContext(), movie.id, movie.title)
        }
        rvWatchlist.adapter = watchlistAdapter
        rvWatchlist.isNestedScrollingEnabled = false

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Load all movies into cache, then load user lists
        FirebaseDatabase.getInstance().getReference("movies")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val cache = mutableMapOf<String, Movie>()
                    snapshot.children.forEach { child ->
                        val m = child.getValue(Movie::class.java) ?: return@forEach
                        m.id = child.key ?: return@forEach
                        cache[m.id] = m
                    }
                    allMoviesCache = cache
                    user?.uid?.let { uid ->
                        loadLikedMovies(uid, tvLikedEmpty)
                        loadWatchlist(uid, tvWatchlistEmpty)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onResume() {
        super.onResume()
        val pending = AdManager.consumePendingMovieId(requireContext())
        if (pending != null) {
            startActivity(Intent(requireContext(), MovieDetailActivity::class.java).apply {
                putExtra("movie_id", pending.first)
                putExtra("movie_title", pending.second)
            })
        }
    }

    private fun loadLikedMovies(uid: String, tvEmpty: TextView) {
        FirebaseDatabase.getInstance().getReference("users/$uid/liked_movies")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val likedIds = snapshot.children.mapNotNull { it.key }
                    val movies = likedIds.mapNotNull { allMoviesCache[it] }
                        .sortedByDescending { it.createdAt }
                    likedAdapter?.updateMovies(movies)
                    tvEmpty.visibility = if (movies.isEmpty()) View.VISIBLE else View.GONE
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadWatchlist(uid: String, tvEmpty: TextView) {
        FirebaseDatabase.getInstance().getReference("users/$uid/watchlist")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val watchIds = snapshot.children.mapNotNull { it.key }
                    val movies = watchIds.mapNotNull { allMoviesCache[it] }
                        .sortedByDescending { it.createdAt }
                    watchlistAdapter?.updateMovies(movies)
                    tvEmpty.visibility = if (movies.isEmpty()) View.VISIBLE else View.GONE
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
