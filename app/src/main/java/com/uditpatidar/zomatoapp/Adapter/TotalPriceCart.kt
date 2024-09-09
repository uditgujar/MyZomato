package com.uditpatidar.zomatoapp.Adapter


import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.uditpatidar.zomatoapp.R
import com.uditpatidar.zomatoapp.ui.Activity.CartActivity

class TotalPriceCart(
    private val dishesList: MutableList<Dish>,
    private val cartActivity: CartActivity,
    private val dishAdapter: DishAdapter // Reference to the DishAdapter
) : RecyclerView.Adapter<TotalPriceCart.DishViewHolder>() {

    private val handler = Handler(Looper.getMainLooper()) // Initialize the Handler

    inner class DishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDishesNameCart: TextView = itemView.findViewById(R.id.tvDishesNameCart)
        val tvCartPrice: TextView = itemView.findViewById(R.id.tvCartPrice)
        val quantityTextCart: TextView = itemView.findViewById(R.id.quantityTextCart)
        val plusButtonCart: ImageView = itemView.findViewById(R.id.plusButtonCart)
        val minusButtonCart: ImageView = itemView.findViewById(R.id.minusButtonCart)

        @SuppressLint("SetTextI18n")
        fun bind(dish: Dish, position: Int) {
            tvDishesNameCart.text = dish.name
            tvCartPrice.text = "₹${dish.price}"

            val sharedPreferences = cartActivity.getSharedPreferences("CartPreferences", Context.MODE_PRIVATE)
            val itemCount = sharedPreferences.getInt(dish.name, 0)
            dish.quantity = itemCount

            quantityTextCart.text = itemCount.toString()

            plusButtonCart.setOnClickListener {
                dish.quantity++
                quantityTextCart.text = dish.quantity.toString()
                handler.post {
                    notifyItemChanged(position)
                    saveCartItems()
                    saveSelectedItems(dish.name, dish.quantity)
                    cartActivity.calculateTotalPrice()
                    dishAdapter.notifyDataSetChanged() // Notify DishAdapter about the change
                }
            }

            minusButtonCart.setOnClickListener {
                handler.post {
                    if (dish.quantity > 1) {
                        dish.quantity--
                        quantityTextCart.text = dish.quantity.toString()
                        saveSelectedItems(dish.name, dish.quantity)
                    } else {
                        // Remove the dish from the list if quantity is 0
                        dishesList.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, dishesList.size)
                        // Remove the dish from SharedPreferences if its quantity is zero
                        removeSelectedItem(dish.name)
                    }
                    saveCartItems()
                    cartActivity.calculateTotalPrice()
                    dishAdapter.notifyDataSetChanged() // Notify DishAdapter about the change
                }
            }
        }

        private fun saveCartItems() {
            val sharedPreferences = itemView.context.getSharedPreferences("CartPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val restaurantId = cartActivity.intent.getStringExtra("RESTAURANT_ID") ?: ""
            val cartItemsJson = Gson().toJson(dishesList.filter { it.quantity > 0 })
            editor.putString("cart_items_$restaurantId", cartItemsJson)
            editor.putInt("total_items_added_$restaurantId", dishesList.sumBy { it.quantity })
            editor.apply()
        }
    }

    private fun saveSelectedItems(name: String, itemCount: Int) {
        val sharedPreferences = cartActivity.getSharedPreferences("CartPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(name, itemCount)
        editor.apply()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cartorder, parent, false)
        return DishViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        val dish = dishesList[position]
        holder.bind(dish, position)
    }

    override fun getItemCount(): Int = dishesList.size

    fun refreshData() {
        handler.post {
            notifyDataSetChanged()
        }
    }

    private fun removeSelectedItem(name: String) {
        val sharedPreferences = cartActivity.getSharedPreferences("CartPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(name)
        editor.apply()
    }
}