package com.aamovies.aamovies.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aamovies.aamovies.R
import com.aamovies.aamovies.model.DownloadLink

class DownloadAdapter(
    private val links: List<DownloadLink>,
    private val onDownloadClick: (DownloadLink) -> Unit
) : RecyclerView.Adapter<DownloadAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val label: TextView = itemView.findViewById(R.id.tv_download_label)
        val size: TextView = itemView.findViewById(R.id.tv_download_size)
        val button: Button = itemView.findViewById(R.id.btn_download)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_download_link, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val link = links[position]
        // Show resolution if available, fall back to label
        holder.label.text = link.resolution.ifEmpty { link.label.ifEmpty { "Download" } }
        holder.size.text = link.size
        holder.size.visibility = if (link.size.isNotEmpty()) View.VISIBLE else View.GONE
        holder.button.setOnClickListener { onDownloadClick(link) }
    }

    override fun getItemCount() = links.size
}
