package com.aamovies.aamovies.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aamovies.aamovies.R
import com.aamovies.aamovies.model.Movie
import com.bumptech.glide.Glide

class MovieAdapter(
    private var movies: List<Movie>,
    private val isHorizontal: Boolean = false,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val poster: ImageView = itemView.findViewById(R.id.img_movie_poster)
        val title: TextView = itemView.findViewById(R.id.tv_movie_title)
        val meta: TextView = itemView.findViewById(R.id.tv_movie_meta)
        val badge: TextView = itemView.findViewById(R.id.tv_quality_badge)
        val pinnedBadge: TextView = itemView.findViewById(R.id.tv_pinned_badge)
        val trendingBadge: TextView = itemView.findViewById(R.id.tv_trending_badge)
        val typeTag: TextView = itemView.findViewById(R.id.tv_type_tag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_card, parent, false)
        if (isHorizontal) {
            val widthPx = (140 * parent.context.resources.displayMetrics.density).toInt()
            view.layoutParams = ViewGroup.MarginLayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT).also {
                it.setMargins(6, 6, 6, 6)
            }
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = movies[position]
        holder.title.text = movie.title
        holder.meta.text = buildString {
            if (movie.year.isNotEmpty()) append(movie.year)
            if (movie.category.isNotEmpty()) {
                if (isNotEmpty()) append(" · ")
                append(movie.category)
            }
        }
        holder.badge.text = movie.quality.ifEmpty { "HD" }
        holder.badge.visibility = View.VISIBLE

        holder.pinnedBadge.visibility = if (movie.pinned) View.VISIBLE else View.GONE
        holder.trendingBadge.visibility = if (movie.trending && !movie.pinned) View.VISIBLE else View.GONE

        if (movie.type == "Series") {
            holder.typeTag.text = "Series"
            holder.typeTag.visibility = View.VISIBLE
        } else {
            holder.typeTag.visibility = View.GONE
        }

        if (movie.poster.isNotEmpty()) {
            Glide.with(holder.poster.context)
                .load(movie.poster)
                .centerCrop()
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.placeholder_movie)
                .into(holder.poster)
        } else {
            holder.poster.setImageResource(R.drawable.placeholder_movie)
        }

        holder.itemView.setOnClickListener { onMovieClick(movie) }
    }

    override fun getItemCount() = movies.size

    fun updateMovies(newMovies: List<Movie>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}
