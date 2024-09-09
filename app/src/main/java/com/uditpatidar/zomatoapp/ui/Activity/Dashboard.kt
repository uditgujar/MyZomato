package com.uditpatidar.zomatoapp.ui.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.uditpatidar.zomatoapp.Adapter.CartAdapter
import com.uditpatidar.zomatoapp.Adapter.CartDetails
import com.uditpatidar.zomatoapp.Adapter.Dish
import com.uditpatidar.zomatoapp.Adapter.DishAdapter
import com.uditpatidar.zomatoapp.R
import com.uditpatidar.zomatoapp.databinding.ActivityDashboardBinding


class Dashboard : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var cartAdapter: CartAdapter
    private lateinit var dishAdapter: DishAdapter
    private lateinit var cartList: MutableList<CartDetails>
    private lateinit var dishes: MutableList<Dish>
    private var showAllItems: Boolean = false // Flag to control showing all items

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)

        // Initialize RecyclerView
        cartList = mutableListOf()
        dishes = mutableListOf()

        dishAdapter = DishAdapter(dishes, RestaurantMenu())
        cartAdapter = CartAdapter(
            cartList,
            { cartDetails -> removeCartItem(cartDetails) },
            { cartDetails ->
                // Handle restaurant click here
                val intent = Intent(this, CartActivity::class.java).apply {
                    putExtra("RESTAURANT_ID", cartDetails.restaurantId)
                }
                startActivity(intent)
            },
            dishAdapter,
            showAllItems // Pass the flag to the adapter
        )

        binding.recycleViewCart.layoutManager = LinearLayoutManager(this)
        binding.recycleViewCart.adapter = cartAdapter

        binding.showMoreLayout.setOnClickListener {
            if (cartList.isNotEmpty()) {
                showAllItems = !showAllItems
                cartAdapter.setShowAllItems(showAllItems)

                if (showAllItems) {

                    binding.showMoreTextView.visibility = View.GONE
                    binding.crossImageView.visibility = View.VISIBLE

                    // Apply layout animation when expanding the items
                    val controller = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down)
                    binding.recycleViewCart.layoutAnimation = controller
                    binding.recycleViewCart.scheduleLayoutAnimation()

                } else {
                    // Show the "Show More" text and hide the cross icon
                    binding.showMoreTextView.visibility = View.VISIBLE
                    binding.crossImageView.visibility = View.GONE
                }

                cartAdapter.notifyDataSetChanged()
            }
        }


        loadCartDetails()
    }


    private fun removeCartItem(cartDetail: CartDetails) {
        val sharedPreferences = getSharedPreferences("CartPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val restaurantId = cartDetail.restaurantId // Use restaurantId as the unique identifier

        // Remove the cart from the cart list and SharedPreferences
        cartList.remove(cartDetail)

        // Clear preferences specific to the restaurant ID
        clearCartPreferences(restaurantId)

        // Apply changes to SharedPreferences
        if (editor.commit()) {
            Log.d("Dashboard", "SharedPreferences updated successfully.")
        } else {
            Log.d("Dashboard", "Failed to update SharedPreferences.")
        }

        // Update the UI
        cartAdapter.notifyDataSetChanged()

        Log.d("Dashboard", "Cart for $restaurantId removed, and preferences cleared.")

    }


    private fun loadCartDetails() {
        val sharedPreferences = getSharedPreferences("CartPreferences", MODE_PRIVATE)
        val allEntries = sharedPreferences.all

        // Clear current cart list
        cartList.clear()

        for ((key, value) in allEntries) {
            if (key.startsWith("cart_items_")) {
                val restaurantId = key.removePrefix("cart_items_")
                val cartItemsJson = value as String
                val totalItems = sharedPreferences.getInt("total_items_added_$restaurantId", 0)
                val restaurantName =
                    sharedPreferences.getString("restaurant_name_$restaurantId", "") ?: ""
                val restaurantImageUrl =
                    sharedPreferences.getString("restaurant_image_url_$restaurantId", "") ?: ""

                if (totalItems > 0 && !cartItemsJson.isNullOrEmpty()) {
                    val type = object : TypeToken<List<Dish>>() {}.type
                    val savedItems: List<Dish> = Gson().fromJson(cartItemsJson, type)

                    cartList.add(
                        CartDetails(
                            restaurantName,
                            totalItems,
                            savedItems,
                            restaurantImageUrl,
                            restaurantId
                        )
                    )
                } else {
                    // Remove empty cart data

                    with(sharedPreferences.edit()) {
                        remove("cart_items_$restaurantId")
                        remove("total_items_added_$restaurantId")
                        remove("restaurant_name_$restaurantId")
                        remove("restaurant_image_url_$restaurantId")
                        apply()
                    }
                }
            }
        }

        if (cartList.isEmpty() || cartList.size==1) {
            binding.showMoreLayout.visibility = View.GONE // Hide the button when no items are in the cart
        } else {
            binding.showMoreLayout.visibility = View.VISIBLE // Show the button when there are items in the cart
        }
        cartAdapter.notifyDataSetChanged()
    }


    private fun clearCartPreferences(restaurantId: String) {
        val sharedPreferences = getSharedPreferences("CartPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Log current state before removal
        Log.d("Dashboard", "Current SharedPreferences data: ${sharedPreferences.all}")

        // Remove data specific to the given restaurant ID (cart)
        editor.remove("cart_items_$restaurantId")
        editor.remove("total_items_added_$restaurantId")
        editor.remove("restaurant_name_$restaurantId")
        editor.remove("restaurant_image_url_$restaurantId")

        // Apply changes to SharedPreferences
        if (editor.commit()) {
            Log.d("Dashboard", "Preferences for cart with restaurant ID $restaurantId cleared.")
        } else {
            Log.d(
                "Dashboard",
                "Failed to clear preferences for cart with restaurant ID $restaurantId."
            )
        }

        // Log state after removal
        Log.d("Dashboard", "SharedPreferences data after removal: ${sharedPreferences.all}")
        dishAdapter.notifyDataSetChanged()
    }


    override fun onResume() {
        super.onResume()
        loadCartDetails()
    }


}
