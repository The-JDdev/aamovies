package com.aamovies.aamovies.adapter

import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChipAdapter(
    private val items: List<String>,
    private val onChipClick: (String) -> Unit
) : RecyclerView.Adapter<ChipAdapter.ViewHolder>() {

    class ViewHolder(val tv: TextView) : RecyclerView.ViewHolder(tv)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val tv = TextView(parent.context).apply {
            val dp8 = (8 * resources.displayMetrics.density).toInt()
            val dp20 = (20 * resources.displayMetrics.density).toInt()
            val dp36 = (36 * resources.displayMetrics.density).toInt()
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp36
            ).also { it.marginEnd = dp8 }
            setPadding(dp20, 0, dp20, 0)
            gravity = Gravity.CENTER
            textSize = 12f
            setTextColor(0xFFFFFFFF.toInt())
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp36.toFloat()
                setColor(0xFF1E1E2E.toInt())
                setStroke((1 * resources.displayMetrics.density).toInt(), 0xFF00a8ff.toInt())
            }
        }
        return ViewHolder(tv)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tv.text = items[position]
        holder.tv.setOnClickListener { onChipClick(items[position]) }
    }

    override fun getItemCount() = items.size
}
