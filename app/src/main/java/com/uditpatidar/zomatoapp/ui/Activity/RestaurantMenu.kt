package com.uditpatidar.zomatoapp.ui.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.uditpatidar.zomatoapp.Adapter.Dish
import com.uditpatidar.zomatoapp.Adapter.DishAdapter
import com.uditpatidar.zomatoapp.Adapter.MenuAdapter
import com.uditpatidar.zomatoapp.Adapter.MenuItems
import com.uditpatidar.zomatoapp.R
import com.uditpatidar.zomatoapp.databinding.ActivityRestaurantMenuBinding


class RestaurantMenu : AppCompatActivity() {
    private lateinit var binding: ActivityRestaurantMenuBinding
    private lateinit var rvMenu: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    lateinit var restaurantId: String
    private var restaurantName: String? = null
    var totalItemsAdded = 0
    private lateinit var dishAdapter: DishAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var dishesList: MutableList<Dish>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        restaurantId = intent.getStringExtra("RESTAURANT_ID") ?: ""
        Log.e("TAG:::::::::::::::::::::::::::::", "$restaurantId")
        firestore = FirebaseFirestore.getInstance()
        dishesList = mutableListOf()
        dishAdapter = DishAdapter(dishesList, this)

        binding.backBar.setOnClickListener {
            saveCartItemsAndNotifyAdapters()
            finish()
        }

        setRestaurantName()

        rvMenu = findViewById(R.id.rvMenu)
        rvMenu.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val menuItems = listOf(
            MenuItems("Andhra Mutton Biryani", "Restaurant A","750/-", R.drawable.frute),
            MenuItems("Paneer Butter Masala", "Restaurant B", "550/-", R.drawable.thali),
            MenuItems("Chicken Tikka", "Restaurant C", "600/-", R.drawable.biryani),
            MenuItems("Veg Pulao", "Restaurant D", "450/-", R.drawable.panir)
        )

        menuAdapter = MenuAdapter(menuItems)
        rvMenu.adapter = menuAdapter

        val recyclerView: RecyclerView = findViewById(R.id.recycleViewVertical)
        recyclerView.layoutManager = LinearLayoutManager(this)

        dishAdapter = DishAdapter(dishesList, this)
        recyclerView.adapter = dishAdapter


        if (restaurantId.isNotEmpty()) {
            fetchDishes()
        } else {
            Log.e("RestaurantMenuActivity", "Invalid restaurant ID")
        }

        loadCartItems() // Load the cart items

        // Initially hide the bottom layout
        binding.bottamMenu.visibility = View.GONE

        binding.bottamMenu.setOnClickListener {
            navigateToCart()
        }
        saveCartItems(this)

    }

    private fun navigateToCart() {
        Log.d("Dashboard", "Item to be deleted: ${restaurantId}")
        val intent = Intent(this, CartActivity::class.java)
        intent.putExtra("RESTAURANT_ID", restaurantId)

       //intent.putExtra("RESTAURANT_NAME", restaurantName) // Pass restaurant name
        startActivity(intent)
    }


    private fun setRestaurantName() {
        val restaurantName = intent.getStringExtra("RESTAURANT_NAME") ?: "Unknown Restaurant"
        binding.RestaurantnameMenu.text = restaurantName
    }

    private fun fetchDishes() {
        firestore.collection("restaurants")
            .document(restaurantId)
            .collection("dishes")
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    Log.e("Firestore", "Error fetching dishes: ", exception)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val tempList = mutableListOf<Dish>()
                    for (document in snapshots.documents) {
                        val name = document.getString("name") ?: ""
                        val description = document.getString("description") ?: ""
                        val price = document.getString("price") ?: ""
                        val imageUrl = document.getString("imageUrl") ?: ""

                        tempList.add(Dish(name, description, price, imageUrl))
                    }


                    dishesList.addAll(tempList)

                    // Now, apply the saved quantities
                    loadCartItems() // Load the cart items from SharedPreferences
                }
            }
    }





    fun saveCartItems(context: Context) {
        val sharedPreferences = context.getSharedPreferences("CartPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val cartItemsJson = Gson().toJson(dishesList.filter { it.quantity > 0 }) // Filter only selected items
        editor.putString("cart_items_$restaurantId", cartItemsJson) // Use restaurantId in the key
        editor.putInt("total_items_added_$restaurantId", totalItemsAdded) // Use restaurantId in the key

        getRestaurantName(restaurantId) { restaurantName ->
            editor.putString("restaurant_name_$restaurantId", restaurantName) // Save restaurant name
            editor.apply()
        }

        getRestaurantImageUrl(restaurantId) { imageUrl ->
            editor.putString("restaurant_image_url_$restaurantId", imageUrl) // Save restaurant image URL
            editor.apply()
        }
    }




    private fun getRestaurantName(restaurantId: String, callback: (String) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("restaurants").document(restaurantId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "Unknown Restaurant"
                    callback(name)
                } else {
                    callback("Restaurant Not Found")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting restaurant name", exception)
                callback("Error")
            }
    }

    private fun getRestaurantImageUrl(restaurantId: String, callback: (String) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("restaurants").document(restaurantId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val imageUrl = document.getString("imageUrl") ?: "default_image_url"
                    callback(imageUrl)
                } else {
                    callback("default_image_url")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting restaurant image URL", exception)
                callback("default_image_url")
            }

    }

    fun updateItemCount(count: Int) {
        totalItemsAdded = count
        binding.itemAddedTextView.text = "$totalItemsAdded items added"

        if (totalItemsAdded > 0) {
            binding.bottamMenu.visibility = View.VISIBLE
        } else {
            binding.bottamMenu.visibility = View.GONE
            dishAdapter.clearAllSelections()  // Clear all selected dishes
        }

        dishAdapter.notifyDataSetChanged()
    }

    fun loadCartItems() {
        val sharedPreferences = getSharedPreferences("CartPreferences", MODE_PRIVATE)
        val cartItemsJson = sharedPreferences.getString(
            "cart_items_$restaurantId",
            null
        ) // Use restaurantId in the key
        val totalItems = sharedPreferences.getInt(
            "total_items_added_$restaurantId",
            0
        ) // Use restaurantId in the key

        if (!cartItemsJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<Dish>>() {}.type
            val savedItems: List<Dish> = Gson().fromJson(cartItemsJson, type)

            for (savedItem in savedItems) {
                val dish = dishesList.find { it.name == savedItem.name }
                dish?.quantity = savedItem.quantity // Restore quantity
            }
            dishAdapter.notifyDataSetChanged()
        }

        totalItemsAdded = totalItems
        updateItemCount(totalItemsAdded)
    }




    private fun checkAndClearDishes() {
        val sharedPreferences = getSharedPreferences("CartPreferences", MODE_PRIVATE)
        val clearDishes = sharedPreferences.getBoolean("clear_dishes_$restaurantId", false)

        if (clearDishes) {
            // Clear the selected dishes
            clearSelectedDishes()

            // Reset the flag in SharedPreferences
            sharedPreferences.edit().putBoolean("clear_dishes_$restaurantId", false).apply()
        }
    }

    private fun clearSelectedDishes() {
        // Clear the quantities of all dishes
        for (dish in dishesList) {
            dish.quantity = 0
        }
        dishAdapter.notifyDataSetChanged()
      //  updateItemCount(0) // Reset item count to 0
    }

    private fun saveCartItemsAndNotifyAdapters() {
        // Save cart items to SharedPreferences
        val sharedPreferences =getSharedPreferences("CartPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val restaurantId = intent.getStringExtra("RESTAURANT_ID") ?: ""
        val cartItemsJson = Gson().toJson(dishesList.filter { it.quantity > 0 })
        editor.putString("cart_items_$restaurantId", cartItemsJson)
        editor.putInt("total_items_added_$restaurantId", dishesList.sumBy { it.quantity })
        editor.apply()

        // Notify both adapters
        dishAdapter.notifyDataSetChanged()

    }






    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            checkAndClearDishes()
            loadCartItems()
            saveCartItems(this)
        }, 100)
    }


    }
