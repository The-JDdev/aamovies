package com.aamovies.aamovies.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aamovies.aamovies.FilteredMoviesActivity
import com.aamovies.aamovies.LikedMoviesActivity
import com.aamovies.aamovies.LoginActivity
import com.aamovies.aamovies.MainActivity
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
import kotlin.math.ceil
import kotlin.math.min

class HomeFragment : Fragment() {

    private val pageSize = 32
    private var currentPage = 0
    private var allLatestMovies: List<Movie> = emptyList()
    private var pinnedMovies: List<Movie> = emptyList()

    private lateinit var scrollViewHome: ScrollView
    private lateinit var rvTrending: RecyclerView
    private lateinit var rvFeatured: RecyclerView
    private lateinit var tvEmptyTrending: TextView
    private lateinit var tvEmptyFeatured: TextView
    private lateinit var hsvPagination: HorizontalScrollView
    private lateinit var llPagination: LinearLayout
    private lateinit var llBrowseCategories: LinearLayout
    private lateinit var llBrowseGenres: LinearLayout
    private lateinit var llBrowseYears: LinearLayout

    private var trendingAdapter: MovieAdapter? = null
    private var featuredAdapter: MovieAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scrollViewHome = view.findViewById(R.id.scroll_view_home)
        rvTrending = view.findViewById(R.id.rv_trending)
        rvFeatured = view.findViewById(R.id.rv_featured)
        tvEmptyTrending = view.findViewById(R.id.tv_empty_trending)
        tvEmptyFeatured = view.findViewById(R.id.tv_empty_featured)
        hsvPagination = view.findViewById(R.id.hsv_pagination)
        llPagination = view.findViewById(R.id.ll_pagination)
        llBrowseCategories = view.findViewById(R.id.ll_browse_categories)
        llBrowseGenres = view.findViewById(R.id.ll_browse_genres)
        llBrowseYears = view.findViewById(R.id.ll_browse_years)

        val btnHamburger = view.findViewById<ImageView>(R.id.btn_hamburger)
        val btnOverflow = view.findViewById<ImageView>(R.id.btn_overflow_menu)

        rvTrending.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvFeatured.layoutManager = GridLayoutManager(requireContext(), 2)
        rvFeatured.isNestedScrollingEnabled = false

        trendingAdapter = MovieAdapter(emptyList(), isHorizontal = true, showTags = false) { movie ->
            handleMovieCardClick(movie)
        }
        featuredAdapter = MovieAdapter(emptyList(), isHorizontal = false, showTags = true) { movie ->
            handleMovieCardClick(movie)
        }
        rvTrending.adapter = trendingAdapter
        rvFeatured.adapter = featuredAdapter

        btnHamburger.setOnClickListener { (activity as? MainActivity)?.openDrawer() }
        btnOverflow.setOnClickListener { v -> showOverflowMenu(v) }

        loadMovies()
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

    private fun loadMovies() {
        FirebaseDatabase.getInstance().getReference("movies")
            .orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val all = mutableListOf<Movie>()
                    snapshot.children.forEach { child ->
                        val movie = child.getValue(Movie::class.java) ?: return@forEach
                        movie.id = child.key ?: return@forEach
                        if (!movie.upcoming) all.add(movie)
                    }

                    // Trending: LIFO — sort by trendingOrder DESC, take 15
                    val trendingList = all
                        .filter { it.trending }
                        .sortedByDescending { it.trendingOrder.takeIf { o -> o > 0 } ?: it.createdAt }
                        .take(15)

                    // Pinned: LIFO — sort by pinnedOrder DESC
                    pinnedMovies = all
                        .filter { it.pinned }
                        .sortedByDescending { it.pinnedOrder.takeIf { o -> o > 0 } ?: it.createdAt }

                    // Latest: non-pinned, sort by createdAt DESC
                    allLatestMovies = all
                        .filter { !it.pinned }
                        .sortedByDescending { it.createdAt }

                    trendingAdapter?.updateMovies(trendingList)
                    tvEmptyTrending.visibility = if (trendingList.isEmpty()) View.VISIBLE else View.GONE

                    // Populate browse sections from actual Firebase data
                    populateBrowseSections(all)

                    currentPage = 0
                    refreshGrid()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /**
     * Build the 3 browse sections at the bottom with actual values from Firebase.
     * These are populated from whatever the admin entered when posting movies.
     */
    private fun populateBrowseSections(allMovies: List<Movie>) {
        if (!isAdded) return

        val categories = allMovies
            .mapNotNull { it.category.trim().takeIf { c -> c.isNotEmpty() } }
            .distinct()
            .sorted()

        val genres = allMovies
            .flatMap { it.genre.split(",").map { g -> g.trim() } }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()

        val years = allMovies
            .mapNotNull { it.year.trim().takeIf { y -> y.isNotEmpty() && y != "0" } }
            .distinct()
            .sortedDescending()

        buildBrowseRow(llBrowseCategories, categories, "category")
        buildBrowseRow(llBrowseGenres, genres, "genre")
        buildBrowseRow(llBrowseYears, years, "year")
    }

    /**
     * Fills a LinearLayout with outline pill TextViews for browse chips.
     * Tapping any chip opens FilteredMoviesActivity for that filter type + value.
     */
    private fun buildBrowseRow(container: LinearLayout, items: List<String>, filterType: String) {
        container.removeAllViews()
        val density = resources.displayMetrics.density

        items.forEach { label ->
            val tv = TextView(requireContext()).apply {
                text = label
                textSize = 13f
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundResource(R.drawable.bg_browse_chip)
                setPadding(
                    (16 * density).toInt(),
                    (8 * density).toInt(),
                    (16 * density).toInt(),
                    (8 * density).toInt()
                )
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.setMargins((4 * density).toInt(), 0, (4 * density).toInt(), 0) }

                setOnClickListener {
                    startActivity(
                        Intent(requireContext(), FilteredMoviesActivity::class.java).apply {
                            putExtra(FilteredMoviesActivity.EXTRA_FILTER_TYPE, filterType)
                            putExtra(FilteredMoviesActivity.EXTRA_FILTER_VALUE, label)
                        }
                    )
                }
            }
            container.addView(tv)
        }
    }

    private fun refreshGrid() {
        if (!isAdded) return
        val start = currentPage * pageSize
        val end = min(start + pageSize, allLatestMovies.size)
        val pageMovies = if (start < allLatestMovies.size) allLatestMovies.subList(start, end) else emptyList()

        // Pinned movies always stay at the top
        val displayMovies = pinnedMovies + pageMovies
        featuredAdapter?.updateMovies(displayMovies)

        val empty = displayMovies.isEmpty()
        tvEmptyFeatured.visibility = if (empty) View.VISIBLE else View.GONE

        val totalPages = if (allLatestMovies.isEmpty()) 0 else ceil(allLatestMovies.size.toDouble() / pageSize).toInt()
        buildPagination(totalPages)
    }

    private fun buildPagination(totalPages: Int) {
        if (!isAdded) return
        llPagination.removeAllViews()
        if (totalPages <= 1) {
            hsvPagination.visibility = View.GONE
            return
        }
        hsvPagination.visibility = View.VISIBLE

        val pages = buildPageList(totalPages, currentPage + 1)
        val density = resources.displayMetrics.density

        pages.forEach { page ->
            if (page == -1) {
                val dots = TextView(requireContext()).apply {
                    text = "…"
                    setTextColor(0xFFAAAAAA.toInt())
                    textSize = 14f
                    setPadding((6 * density).toInt(), (8 * density).toInt(), (6 * density).toInt(), (8 * density).toInt())
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.setMargins((2 * density).toInt(), 0, (2 * density).toInt(), 0) }
                }
                llPagination.addView(dots)
            } else {
                val isActive = page == (currentPage + 1)
                val tv = TextView(requireContext()).apply {
                    text = page.toString()
                    textSize = 13f
                    setTextColor(if (isActive) 0xFF000000.toInt() else 0xFF00a8ff.toInt())
                    setBackgroundResource(if (isActive) R.drawable.bg_pagination_active else R.drawable.bg_pagination_inactive)
                    setPadding((14 * density).toInt(), (8 * density).toInt(), (14 * density).toInt(), (8 * density).toInt())
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.setMargins((3 * density).toInt(), 0, (3 * density).toInt(), 0) }
                    setOnClickListener {
                        currentPage = page - 1
                        refreshGrid()
                        hsvPagination.post { hsvPagination.scrollTo(0, 0) }
                    }
                }
                llPagination.addView(tv)
            }
        }
    }

    private fun buildPageList(total: Int, current: Int): List<Int> {
        if (total <= 8) return (1..total).toList()
        val pages = mutableListOf<Int>()
        val surrounding = (maxOf(1, current - 1)..minOf(total, current + 1)).toList()
        val allVisible = (listOf(1, 2) + surrounding + listOf(total - 1, total)).distinct().sorted()
        var prev = 0
        for (p in allVisible) {
            if (p - prev > 1) pages.add(-1)
            pages.add(p)
            prev = p
        }
        return pages
    }

    private fun showOverflowMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.home_overflow_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_liked -> {
                    startActivity(Intent(requireContext(), LikedMoviesActivity::class.java)); true
                }
                R.id.menu_contact -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("jdvijay.me@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "Support: AAMovies 2.0")
                    }
                    if (intent.resolveActivity(requireContext().packageManager) != null) startActivity(intent)
                    else Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_profile -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment())
                        .addToBackStack(null).commit(); true
                }
                R.id.menu_signout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent); true
                }
                else -> false
            }
        }
        popup.show()
    }
}
