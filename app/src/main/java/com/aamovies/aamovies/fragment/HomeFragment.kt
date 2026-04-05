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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var rvTrending: RecyclerView
    private lateinit var rvFeatured: RecyclerView
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
        tvEmptyTrending = view.findViewById(R.id.tv_empty_trending)
        tvEmptyFeatured = view.findViewById(R.id.tv_empty_featured)

        rvTrending.layoutManager = GridLayoutManager(requireContext(), 2)
        rvFeatured.layoutManager = GridLayoutManager(requireContext(), 2)

        trendingAdapter = MovieAdapter(emptyList()) { movie -> openDetail(movie) }
        featuredAdapter = MovieAdapter(emptyList()) { movie -> openDetail(movie) }
        rvTrending.adapter = trendingAdapter
        rvFeatured.adapter = featuredAdapter

        loadMovies()
    }

    private fun loadMovies() {
        FirebaseDatabase.getInstance().getReference("movies")
            .orderByChild("createdAt")
            .limitToLast(20)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val trending = mutableListOf<Movie>()
                    val featured = mutableListOf<Movie>()
                    snapshot.children.forEach { child ->
                        val movie = child.getValue(Movie::class.java) ?: return@forEach
                        movie.id = child.key ?: return@forEach
                        if (movie.trending) trending.add(0, movie)
                        else featured.add(0, movie)
                    }
                    val allRecent = (trending + featured).take(20)
                    trendingAdapter?.updateMovies(allRecent.filter { it.trending })
                    featuredAdapter?.updateMovies(allRecent.take(10))
                    tvEmptyTrending.visibility = if (trending.isEmpty()) View.VISIBLE else View.GONE
                    tvEmptyFeatured.visibility = if (featured.isEmpty()) View.VISIBLE else View.GONE
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
