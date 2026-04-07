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
    private val showTags: Boolean = true,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val poster: ImageView = itemView.findViewById(R.id.img_movie_poster)
        val title: TextView = itemView.findViewById(R.id.tv_movie_title)
        val meta: TextView = itemView.findViewById(R.id.tv_movie_meta)
        val tagRating: TextView = itemView.findViewById(R.id.tv_tag_rating)
        val tagQuality: TextView = itemView.findViewById(R.id.tv_tag_quality)
        val tagLanguage: TextView = itemView.findViewById(R.id.tv_tag_language)
        val tagType: TextView = itemView.findViewById(R.id.tv_tag_type)
        val pinBadge: TextView = itemView.findViewById(R.id.tv_pin_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_card, parent, false)
        if (isHorizontal) {
            val widthPx = (140 * parent.context.resources.displayMetrics.density).toInt()
            view.layoutParams = ViewGroup.MarginLayoutParams(
                widthPx, ViewGroup.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(5, 5, 5, 5) }
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = movies[position]

        holder.title.text = movie.title

        // Meta row (year · category) — show only in full-card grid
        if (!isHorizontal && showTags) {
            val meta = buildString {
                if (movie.year.isNotEmpty()) append(movie.year)
                if (movie.category.isNotEmpty()) {
                    if (isNotEmpty()) append(" · ")
                    append(movie.category)
                }
            }
            if (meta.isNotEmpty()) {
                holder.meta.text = meta
                holder.meta.visibility = View.VISIBLE
            } else {
                holder.meta.visibility = View.GONE
            }
        } else {
            holder.meta.visibility = View.GONE
        }

        // Overlay tags — shown only in grid modes (not horizontal trending)
        val displayTags = showTags && !isHorizontal
        if (displayTags) {
            // Rating tag
            if (movie.rating.isNotEmpty()) {
                holder.tagRating.text = "★ ${movie.rating}"
                holder.tagRating.visibility = View.VISIBLE
            } else {
                holder.tagRating.visibility = View.GONE
            }
            // Quality tag
            if (movie.quality.isNotEmpty()) {
                holder.tagQuality.text = movie.quality.uppercase()
                holder.tagQuality.visibility = View.VISIBLE
            } else {
                holder.tagQuality.visibility = View.GONE
            }
            // Language tag
            if (movie.language.isNotEmpty()) {
                holder.tagLanguage.text = movie.language.uppercase()
                holder.tagLanguage.visibility = View.VISIBLE
            } else {
                holder.tagLanguage.visibility = View.GONE
            }
            // Type tag
            if (movie.type.isNotEmpty()) {
                holder.tagType.text = movie.type.uppercase()
                holder.tagType.visibility = View.VISIBLE
            } else {
                holder.tagType.visibility = View.GONE
            }
        } else {
            holder.tagRating.visibility = View.GONE
            holder.tagQuality.visibility = View.GONE
            holder.tagLanguage.visibility = View.GONE
            holder.tagType.visibility = View.GONE
        }

        // Pinned badge
        holder.pinBadge.visibility = if (movie.pinned) View.VISIBLE else View.GONE

        // Load poster
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
