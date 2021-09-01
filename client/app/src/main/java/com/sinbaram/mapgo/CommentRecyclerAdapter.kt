package com.sinbaram.mapgo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CommentRecyclerAdapter internal constructor(var context: Context, list: MutableList<Map<String, String>>?) :
    RecyclerView.Adapter<CommentRecyclerAdapter.ViewHolder>() {
    private var mData: MutableList<Map<String, String>>? = null

    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var content: TextView
        var writer: TextView
        var image: ImageView

        init {
            content = itemView.findViewById(R.id.commentContent)
            writer = itemView.findViewById(R.id.commentWriter)
            image = itemView.findViewById(R.id.commentImage)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val context: Context = parent.context
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.comment_recycler, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.writer.text = mData!![position]["writer"]
        holder.content.text = mData!![position]["contents"]
        Glide.with(context).load(mData!![position]["writerImage"]).into(holder.image)
    }

    override fun getItemCount(): Int {
        return mData!!.size
    }

    init {
        mData = list
    }
}
