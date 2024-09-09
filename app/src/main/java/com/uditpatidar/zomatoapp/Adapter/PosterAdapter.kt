package com.uditpatidar.zomatoapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

import com.uditpatidar.zomatoapp.R
import com.uditpatidar.zomatoapp.databinding.ItemPosterBinding
import com.uditpatidar.zomatoapp.ui.Dataclass.dataposet

class PosterAdapter(var context: Context, var array: List<dataposet>, var viewPager: ViewPager2) : RecyclerView.Adapter<PosterAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_poster, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return array.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(array[position])
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val posterImage = itemView.findViewById<ImageView>(R.id.poster)

        fun bind(data: dataposet) {
            posterImage.setImageResource(data.imageposter)
        }
    }
}