package com.aamovies.aamovies.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.aamovies.aamovies.R
import com.bumptech.glide.Glide

class ScreenshotAdapter(
    private val screenshots: List<String>
) : RecyclerView.Adapter<ScreenshotAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.img_screenshot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_screenshot, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lp = holder.itemView.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) {
            lp.isFullSpan = (screenshots.size == 1)
        }
        Glide.with(holder.image.context)
            .load(screenshots[position])
            .fitCenter()
            .placeholder(R.drawable.placeholder_movie)
            .into(holder.image)
    }

    override fun getItemCount() = screenshots.size
}
