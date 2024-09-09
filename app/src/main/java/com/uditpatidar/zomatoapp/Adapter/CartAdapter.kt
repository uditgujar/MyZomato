package com.uditpatidar.zomatoapp.Adapter


import android.app.AlertDialog
import android.content.Context


import android.content.SharedPreferences
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso
import com.uditpatidar.zomatoapp.R


/*
class CartAdapter(
    private val cartList: List<CartDetails>,
    private val onItemDelete: (CartDetails) -> Unit,

    private val onRestaurantClickCart: (CartDetails) -> Unit,
    private val dishAdapter: DishAdapter,



) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val restaurantImageView: ImageView = itemView.findViewById(R.id.ivRestaurantImage)
        val restaurantNameTextView: TextView = itemView.findViewById(R.id.tvCartname)
        val totalItemsTextView: TextView = itemView.findViewById(R.id.totalQuantity)
        val mcvCross: MaterialCardView = itemView.findViewById(R.id.mcvCross)
        val cvViewCart: CardView = itemView.findViewById(R.id.cvViewCart)


        init {
            cvViewCart.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val restaurant = cartList[position]
                    onRestaurantClickCart(restaurant)
                }
            }
        }

        fun bind(cartDetails: CartDetails) {
            restaurantNameTextView.text = cartDetails.restaurantName
            totalItemsTextView.text = "${cartDetails.totalItems} items added"

            // Load the restaurant image using Picasso
            val imageUrl = cartDetails.restaurantImageUrl

            // Load the restaurant image using Picasso
            if (!imageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.hotel1) // Optional placeholder image
                    .error(R.drawable.error) // Optional error image
                    .into(restaurantImageView)
            } else {
                // Set a default or placeholder image if the URL is empty
                restaurantImageView.setImageResource(R.drawable.error) // Use a default image resource
            }
            mcvCross.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val cartDetails = cartList[position]

                    // Show confirmation dialog
                    val dialogView = LayoutInflater.from(itemView.context)
                        .inflate(R.layout.item_deletedialog, null)
                    val dialog = AlertDialog.Builder(itemView.context)
                        .setView(dialogView)
                        .create()

                    dialogView.findViewById<CardView>(R.id.cvNo).setOnClickListener {
                        dialog.dismiss()
                    }

                    dialogView.findViewById<CardView>(R.id.cvYes).setOnClickListener {
                        // Handle "Yes" response
                        onItemDelete(cartDetails)

                        // Reset quantities of dishes associated with the removed cart


                        dialog.dismiss()
                    }

                    dialog.show()

                    // Set the dialog width and height
                    dialog.window?.apply {
                        setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        setBackgroundDrawableResource(android.R.color.transparent)
                        attributes = attributes.apply {
                            gravity = Gravity.CENTER
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartList[position])

    }

    override fun getItemCount(): Int {
        return cartList.size
    }


}
*/
class CartAdapter(
    private val cartList: List<CartDetails>,
    private val onItemDelete: (CartDetails) -> Unit,
    private val onRestaurantClickCart: (CartDetails) -> Unit,
    private val dishAdapter: DishAdapter,
    private var showAllItems: Boolean = false // Flag to control showing all items
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val restaurantImageView: ImageView = itemView.findViewById(R.id.ivRestaurantImage)
        val restaurantNameTextView: TextView = itemView.findViewById(R.id.tvCartname)
        val totalItemsTextView: TextView = itemView.findViewById(R.id.totalQuantity)
        val mcvCross: MaterialCardView = itemView.findViewById(R.id.mcvCross)
        val cvViewCart: CardView = itemView.findViewById(R.id.cvViewCart)

        init {
            cvViewCart.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val restaurant = cartList[position]
                    onRestaurantClickCart(restaurant)
                }
            }
        }

        fun bind(cartDetails: CartDetails) {
            restaurantNameTextView.text = cartDetails.restaurantName
            totalItemsTextView.text = "${cartDetails.totalItems} items added"

            // Load the restaurant image using Picasso
            val imageUrl = cartDetails.restaurantImageUrl
            if (!imageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.hotel1) // Optional placeholder image
                    .error(R.drawable.error) // Optional error image
                    .into(restaurantImageView)
            } else {
                restaurantImageView.setImageResource(R.drawable.error)
            }

            mcvCross.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val cartDetails = cartList[position]
                    showDeleteConfirmationDialog(cartDetails)
                }
            }
        }

        private fun showDeleteConfirmationDialog(cartDetails: CartDetails) {
            val dialogView = LayoutInflater.from(itemView.context)
                .inflate(R.layout.item_deletedialog, null)
            val dialog = AlertDialog.Builder(itemView.context)
                .setView(dialogView)
                .create()

            dialogView.findViewById<CardView>(R.id.cvNo).setOnClickListener {
                dialog.dismiss()
            }

            dialogView.findViewById<CardView>(R.id.cvYes).setOnClickListener {
                onItemDelete(cartDetails)
                dialog.dismiss()
            }

            dialog.show()
            dialog.window?.apply {
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawableResource(android.R.color.transparent)
                attributes = attributes.apply { gravity = Gravity.CENTER }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        if (cartList.isNotEmpty()) {
            holder.bind(cartList[position])
        }
    }

    override fun getItemCount(): Int {
        return if (cartList.isEmpty()) {
            0 // Return 0 if the cartList is empty
        } else {
            if (showAllItems) cartList.size else 1
        }
    }


    fun setShowAllItems(showAll: Boolean) {
        this.showAllItems = showAll
    }
}

data class CartDetails(
    val restaurantName: String,
    var totalItems: Int,
    val dishes: List<Dish>,
    val restaurantImageUrl: String ,
    var restaurantId:String// Add this line
)

