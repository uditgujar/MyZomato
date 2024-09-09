package com.uditpatidar.zomatoapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.uditpatidar.zomatoapp.R
import com.uditpatidar.zomatoapp.ui.Dataclass.dishesDataClass

class adapterphoto(var context: Context, var array: ArrayList<dishesDataClass>)
    : RecyclerView.Adapter<adapterphoto.MyViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_dishes,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return array.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.posterImage.setImageResource(array[position].Image)
        holder.photographyitem.text =array[position].Name

    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        val photographyitem: TextView =itemView.findViewById(R.id.photographyitem)
        val posterImage: ImageView =itemView.findViewById(R.id.photography)
    }
}
