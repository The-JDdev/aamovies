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
import com.aamovies.aamovies.util.AdManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchFragment : Fragment() {

    private lateinit var etSearch: EditText
    private lateinit var rvResults: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var chipAll: TextView
    private lateinit var chipMovie: TextView
    private lateinit var chipSeries: TextView

    private var searchAdapter: MovieAdapter? = null
    private var allMovies: List<Movie> = emptyList()
    private var typeFilter: String = "All"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etSearch = view.findViewById(R.id.et_search)
        rvResults = view.findViewById(R.id.rv_search_results)
        tvEmpty = view.findViewById(R.id.tv_search_empty)
        chipAll = view.findViewById(R.id.chip_all)
        chipMovie = view.findViewById(R.id.chip_movie)
        chipSeries = view.findViewById(R.id.chip_series)

        rvResults.layoutManager = GridLayoutManager(requireContext(), 2)
        searchAdapter = MovieAdapter(emptyList(), isHorizontal = false, showTags = true) { movie ->
            AdManager.handleMovieCardClick(requireContext(), movie.id, movie.title)
        }
        rvResults.adapter = searchAdapter

        chipAll.setOnClickListener { setTypeFilter("All") }
        chipMovie.setOnClickListener { setTypeFilter("Movie") }
        chipSeries.setOnClickListener { setTypeFilter("Series") }

        FirebaseDatabase.getInstance().getReference("movies")
            .orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val movies = mutableListOf<Movie>()
                    snapshot.children.forEach { child ->
                        val m = child.getValue(Movie::class.java) ?: return@forEach
                        m.id = child.key ?: return@forEach
                        if (!m.upcoming) movies.add(0, m)
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

    private fun setTypeFilter(filter: String) {
        typeFilter = filter
        val activeColor = 0xFF000000.toInt()
        val inactiveColor = 0xFF00a8ff.toInt()
        chipAll.setTextColor(if (filter == "All") activeColor else inactiveColor)
        chipMovie.setTextColor(if (filter == "Movie") activeColor else inactiveColor)
        chipSeries.setTextColor(if (filter == "Series") activeColor else inactiveColor)
        chipAll.setBackgroundResource(if (filter == "All") R.drawable.bg_pagination_active else R.drawable.bg_pagination_inactive)
        chipMovie.setBackgroundResource(if (filter == "Movie") R.drawable.bg_pagination_active else R.drawable.bg_pagination_inactive)
        chipSeries.setBackgroundResource(if (filter == "Series") R.drawable.bg_pagination_active else R.drawable.bg_pagination_inactive)
        filterResults(etSearch.text.toString())
    }

    private fun filterResults(query: String) {
        val typeFiltered = when (typeFilter) {
            "Movie" -> allMovies.filter { it.type.equals("Movie", ignoreCase = true) }
            "Series" -> allMovies.filter { it.type.equals("Series", ignoreCase = true) }
            else -> allMovies
        }
        val filtered = if (query.isBlank()) typeFiltered
        else typeFiltered.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true) ||
            it.language.contains(query, ignoreCase = true) ||
            it.genre.contains(query, ignoreCase = true)
        }
        searchAdapter?.updateMovies(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}
