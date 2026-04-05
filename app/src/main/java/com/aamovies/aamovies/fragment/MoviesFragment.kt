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

class MoviesFragment : Fragment() {

    private lateinit var rvMovies: RecyclerView
    private lateinit var tvEmpty: TextView
    private var movieAdapter: MovieAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_movies, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvMovies = view.findViewById(R.id.rv_all_movies)
        tvEmpty = view.findViewById(R.id.tv_all_empty)

        rvMovies.layoutManager = GridLayoutManager(requireContext(), 2)
        movieAdapter = MovieAdapter(emptyList()) { movie -> openDetail(movie) }
        rvMovies.adapter = movieAdapter

        FirebaseDatabase.getInstance().getReference("movies")
            .orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val movies = mutableListOf<Movie>()
                    snapshot.children.forEach { child ->
                        val m = child.getValue(Movie::class.java) ?: return@forEach
                        m.id = child.key ?: return@forEach
                        movies.add(0, m)
                    }
                    movieAdapter?.updateMovies(movies)
                    tvEmpty.visibility = if (movies.isEmpty()) View.VISIBLE else View.GONE
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
