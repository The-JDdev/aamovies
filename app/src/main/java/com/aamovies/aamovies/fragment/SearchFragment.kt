package com.aamovies.aamovies.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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

class SearchFragment : Fragment() {

    private lateinit var etSearch: EditText
    private lateinit var rvResults: RecyclerView
    private lateinit var tvEmpty: TextView
    private var searchAdapter: MovieAdapter? = null
    private var allMovies: List<Movie> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etSearch = view.findViewById(R.id.et_search)
        rvResults = view.findViewById(R.id.rv_search_results)
        tvEmpty = view.findViewById(R.id.tv_search_empty)

        rvResults.layoutManager = GridLayoutManager(requireContext(), 2)
        searchAdapter = MovieAdapter(emptyList()) { movie -> openDetail(movie) }
        rvResults.adapter = searchAdapter

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
                    allMovies = movies
                    filterResults(etSearch.text.toString())
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterResults(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterResults(query: String) {
        val filtered = if (query.isBlank()) allMovies
        else allMovies.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true) ||
            it.language.contains(query, ignoreCase = true)
        }
        searchAdapter?.updateMovies(filtered)
        tvEmpty.visibility = if (filtered.isEmpty() && query.isNotBlank()) View.VISIBLE else View.GONE
    }

    private fun openDetail(movie: Movie) {
        val intent = Intent(requireContext(), MovieDetailActivity::class.java).apply {
            putExtra("movie_id", movie.id)
            putExtra("movie_title", movie.title)
        }
        startActivity(intent)
    }
}
