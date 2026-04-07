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
import com.aamovies.aamovies.util.AdManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TrendingFragment : Fragment() {

    private var trendingAdapter: MovieAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_trending, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvTrending = view.findViewById<RecyclerView>(R.id.rv_all_trending)
        val tvEmpty = view.findViewById<TextView>(R.id.tv_trending_empty)

        rvTrending.layoutManager = GridLayoutManager(requireContext(), 2)
        trendingAdapter = MovieAdapter(emptyList(), isHorizontal = false, showTags = true) { movie ->
            AdManager.handleMovieCardClick(requireContext(), movie.id, movie.title)
        }
        rvTrending.adapter = trendingAdapter

        FirebaseDatabase.getInstance().getReference("movies")
            .orderByChild("trending")
            .equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val list = mutableListOf<Movie>()
                    snapshot.children.forEach { child ->
                        val movie = child.getValue(Movie::class.java) ?: return@forEach
                        movie.id = child.key ?: return@forEach
                        list.add(movie)
                    }
                    // LIFO: sort by trendingOrder DESC (newest trending = #1)
                    val sorted = list.sortedByDescending { it.trendingOrder.takeIf { o -> o > 0 } ?: it.createdAt }
                    trendingAdapter?.updateMovies(sorted)
                    tvEmpty.visibility = if (sorted.isEmpty()) View.VISIBLE else View.GONE
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
}
