package com.sinbaram.mapgo

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageSliderAdapter(val context: Context, imageList: MutableList<String>) : RecyclerView.Adapter<ImageSliderAdapter.PagerViewHolder>() {
    var imagelist = imageList
    inner class PagerViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder
        (LayoutInflater.from(parent.context).inflate(R.layout.image_viewpager, parent, false)) {
        val image = itemView.findViewById<ImageView>(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PagerViewHolder((parent))

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        Glide.with(context).load(imagelist[position]).into(holder.image)

    }

    override fun getItemCount(): Int = imagelist.size
}
