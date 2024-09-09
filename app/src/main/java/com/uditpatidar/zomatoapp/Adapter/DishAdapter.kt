package com.uditpatidar.zomatoapp.Adapter

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.uditpatidar.zomatoapp.R
import com.uditpatidar.zomatoapp.ui.Activity.RestaurantMenu

class DishAdapter(
    private var dishes: List<Dish>,
    private val activity: RestaurantMenu
) : RecyclerView.Adapter<DishAdapter.DishViewHolder>() {
    private val selectedDishes = mutableSetOf<Int>()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    class DishViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dishImage: ImageView = view.findViewById(R.id.dishImagevertical)
        val dishName: TextView = view.findViewById(R.id.dishName)
        val dishDescription: TextView = view.findViewById(R.id.cartRestaurantName)
        val dishPrice: TextView = view.findViewById(R.id.dishPrice)
        val addButton: CardView = view.findViewById(R.id.cvAdd)
        val quantityOptions: CardView = view.findViewById(R.id.quantityOptions)
        val minusButton: ImageView = view.findViewById(R.id.minusButton)
        val plusButton: ImageView = view.findViewById(R.id.plusButton)
        val quantityText: TextView = view.findViewById(R.id.quantityText)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menuvertical, parent, false)
        return DishViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        val dish = dishes[position]
        holder.dishName.text = dish.name
        holder.dishDescription.text = dish.description
        holder.dishPrice.text = dish.price


        val sharedPreferences = activity.getSharedPreferences("CartPreferences", MODE_PRIVATE)
        val itemCount = sharedPreferences.getInt(dish.name, 0)
        dish.quantity = itemCount
        holder.quantityText.text = itemCount.toString()
        Log.e(TAG, "onBindViewHolder: " + itemCount)
        if (itemCount == 0) {

            holder.addButton.visibility = View.GONE
            holder.quantityOptions.visibility = View.VISIBLE

        } else {
            holder.addButton.visibility = View.VISIBLE
            holder.quantityOptions.visibility = View.GONE
        }

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(dish.imageUrl)
            .placeholder(R.drawable.error)
            .error(R.drawable.error)
            .into(holder.dishImage)

        // Initialize quantity and visibility
        var quantity = dish.quantity
        updateQuantity(holder, quantity)
        holder.quantityOptions.visibility = if (quantity > 0) View.VISIBLE else View.GONE
        holder.addButton.visibility = if (quantity > 0) View.GONE else View.VISIBLE

        holder.addButton.setOnClickListener {
            dish.quantity++
            updateQuantity(holder, dish.quantity)
            holder.quantityOptions.visibility = View.VISIBLE
            holder.addButton.visibility = View.GONE
            activity.updateItemCount(activity.totalItemsAdded + 1)
            activity.saveCartItems(activity)
            saveSelectedItems(dish.name, dish.quantity) // Save the cart items
            notifyDataSetChanged()
        }

        holder.minusButton.setOnClickListener {
            if (dish.quantity > 0) {
                dish.quantity--
                updateQuantity(holder, dish.quantity)
                activity.updateItemCount(activity.totalItemsAdded - 1)
                if (dish.quantity == 0) {
                    holder.quantityOptions.visibility = View.GONE
                    holder.addButton.visibility = View.VISIBLE
                }
                saveSelectedItems(dish.name, dish.quantity)
                activity.saveCartItems(activity) // Save the cart items
                notifyDataSetChanged()
            }
        }

        holder.plusButton.setOnClickListener {
            dish.quantity++
            updateQuantity(holder, dish.quantity)
            holder.quantityOptions.visibility = View.VISIBLE
            holder.addButton.visibility = View.GONE
            activity.updateItemCount(activity.totalItemsAdded + 1)
            activity.saveCartItems(activity)
            saveSelectedItems(dish.name, dish.quantity) // Save the cart items
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = dishes.size

    private fun updateQuantity(holder: DishViewHolder, quantity: Int) {
        holder.quantityText.text = quantity.toString()
    }


    private fun saveSelectedItems(name: String, itemCount: Int) {
        val sharedPreferences = activity.getSharedPreferences("CartPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putInt(name, itemCount)


        editor.apply()
    }

    fun clearAllSelections() {
        for (dish in dishes) {
            dish.quantity = 0
            saveSelectedItems(dish.name, 0)  // Update SharedPreferences
        }
        notifyDataSetChanged()  // Refresh the adapter
    }


}


data class Dish(
    val name: String,
    val description: String,
    val price: String,
    val imageUrl: String,
    var quantity: Int = 0,



)
