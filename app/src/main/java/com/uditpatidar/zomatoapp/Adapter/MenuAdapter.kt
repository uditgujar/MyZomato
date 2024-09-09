package com.uditpatidar.zomatoapp.Adapter

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.uditpatidar.zomatoapp.R



class MenuAdapter(private val menuItems: List<MenuItems>) :
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = menuItems[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = menuItems.size

    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dishName: TextView = itemView.findViewById(R.id.tvdishesNameMenu)
        private val restaurantName: TextView = itemView.findViewById(R.id.tvRestaurantNameMenu)
        private val price: TextView = itemView.findViewById(R.id.tvPrice)
        private val dishImage: ImageView = itemView.findViewById(R.id.dishImage)
       /* private val addButton: CardView = itemView.findViewById(R.id.cvadd)
        private val quantityOptions: RelativeLayout = itemView.findViewById(R.id.quantity_options)
        private val minusButton: TextView = itemView.findViewById(R.id.minus_button)
        private val plusButton: TextView = itemView.findViewById(R.id.plus_button)
        private val quantityText: TextView = itemView.findViewById(R.id.quantity_text)*/


        private var quantity = 0

        fun bind(item: MenuItems) {
            dishName.text = item.dishName
            restaurantName.text = item.restaurantName
            price.text = item.price
            dishImage.setImageResource(item.imageResId)

            // Update visibility based on quantity
            /*  updateVisibility()*/

            /*addButton.setOnClickListener {
                if (quantity == 0) {
                    quantity = 1
                    updateQuantity()
                    updateVisibility()
                } else {
                    quantityOptions.visibility = if (quantityOptions.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                }
            }

            minusButton.setOnClickListener {
                if (quantity > 0) {
                    quantity--
                    updateQuantity()
                    updateVisibility()
                }
            }

            plusButton.setOnClickListener {
                quantity++
                updateQuantity()
                updateVisibility()
            }
        }

        private fun updateQuantity() {
            quantityText.text = quantity.toString()
        }

        private fun updateVisibility() {
            if (quantity > 0) {
                quantityOptions.visibility = View.VISIBLE
                addButton.visibility = View.GONE
            } else {
                quantityOptions.visibility = View.GONE
                addButton.visibility = View.VISIBLE
            }
        }*/
        }}
}

data class MenuItems(
    val dishName: String,
    val restaurantName: String,
    val price: String,
    val imageResId: Int
)
