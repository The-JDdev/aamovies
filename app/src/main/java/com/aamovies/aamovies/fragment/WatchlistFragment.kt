package com.aamovies.aamovies.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aamovies.aamovies.MovieDetailActivity
import com.aamovies.aamovies.R
import com.aamovies.aamovies.adapter.MovieAdapter
import com.aamovies.aamovies.model.Movie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WatchlistFragment : Fragment() {

    private lateinit var rvWatchlist: RecyclerView
    private lateinit var tvEmpty: TextView
    private var watchlistAdapter: MovieAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_watchlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvWatchlist = view.findViewById(R.id.rv_watchlist)
        tvEmpty = view.findViewById(R.id.tv_watchlist_empty)

        rvWatchlist.layoutManager = GridLayoutManager(requireContext(), 2)
        watchlistAdapter = MovieAdapter(emptyList()) { movie -> openDetail(movie) }
        rvWatchlist.adapter = watchlistAdapter

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            tvEmpty.text = "Sign in to view your watchlist"
            tvEmpty.visibility = View.VISIBLE
            return
        }

        FirebaseDatabase.getInstance().getReference("users/$uid/watchlist")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val movieIds = snapshot.children.mapNotNull { it.key }.toList()
                    if (movieIds.isEmpty()) {
                        watchlistAdapter?.updateMovies(emptyList())
                        tvEmpty.visibility = View.VISIBLE
                        return
                    }
                    val movies = mutableListOf<Movie>()
                    var loaded = 0
                    movieIds.forEach { id ->
                        FirebaseDatabase.getInstance().getReference("movies/$id").get()
                            .addOnSuccessListener { s ->
                                val m = s.getValue(Movie::class.java)
                                if (m != null) { m.id = id; movies.add(m) }
                                loaded++
                                if (loaded == movieIds.size && isAdded) {
                                    watchlistAdapter?.updateMovies(movies)
                                    tvEmpty.visibility = if (movies.isEmpty()) View.VISIBLE else View.GONE
                                }
                            }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun openDetail(movie: Movie) {
        val intent = Intent(requireContext(), MovieDetailActivity::class.java).apply {
            putExtra("movie_id", movie.id)
            putExtra("movie_title", movie.title)
        }
        startActivity(intent)
    }
}
