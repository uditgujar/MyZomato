package com.uditpatidar.zomatoapp.ui.Activity

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.RemoteViews
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.uditpatidar.zomatoapp.Adapter.CouponsAdapter
import com.uditpatidar.zomatoapp.Adapter.Dish
import com.uditpatidar.zomatoapp.Adapter.DishAdapter
import com.uditpatidar.zomatoapp.Adapter.TotalPriceCart
import com.uditpatidar.zomatoapp.R
import com.uditpatidar.zomatoapp.databinding.ActivityCartBinding
import com.uditpatidar.zomatoapp.ui.Dataclass.Coupon

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var tvSavedAmount: TextView
    private lateinit var rvCartDishes: RecyclerView
    private lateinit var totalPriceCart: TotalPriceCart
    private lateinit var tvTotalPrice: TextView
    private lateinit var dishesList: MutableList<Dish>
    private var totalPrice: Double = 0.0
        lateinit var restaurantId: String
    private var appliedCouponValue: Double = 0.0
    private lateinit var couponsAdapter: CouponsAdapter
    private lateinit var couponResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var dishAdapter: DishAdapter
    private var selectedCouponId: String? = null
    var value = ""




     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        restaurantId = intent.getStringExtra("RESTAURANT_ID") ?: return

        // Initialize the TextView
        tvSavedAmount = findViewById(R.id.tvSavedAmount)
        tvTotalPrice = findViewById(R.id.tvTotalPrice) // Ensure this is initialized




         couponResultLauncher = registerForActivityResult(
             ActivityResultContracts.StartActivityForResult()
         ) { result: ActivityResult ->
             if (result.resultCode == RESULT_OK) {
                 appliedCouponValue = result.data?.getDoubleExtra("APPLIED_COUPON_VALUE", 0.0) ?: 0.0
                 val appliedCouponCode = result.data?.getStringExtra("APPLIED_COUPON_CODE") ?: "" // Get the coupon code
                 value = appliedCouponValue.toString()

                 Log.e("The Price At Cart", "$appliedCouponValue")

                 totalPrice -= appliedCouponValue
                 Log.e("The Price At Cart", "$totalPrice")

                 findViewById<RelativeLayout>(R.id.rlapplyedcoupon).visibility = View.VISIBLE
                 binding.lottieAnimationViewcv.visibility = View.VISIBLE

                 updateTotalPrice()

                 // Update the saved amount TextView with coupon code
                 val savedAmountText = "You saved ₹$appliedCouponValue with '${appliedCouponCode}'"
                 tvSavedAmount.text = savedAmountText

                 saveCouponData(appliedCouponValue, appliedCouponCode) // Save the coupon data with code

                 // Delay hiding the animation
                 Handler(Looper.getMainLooper()).postDelayed({
                     binding.lottieAnimationViewcv.visibility = View.GONE
                 }, 3000) // 2000 milliseconds = 2 seconds
             }
         }

// Load any previously applied coupon data
         loadCouponData()



         // Load any previously applied coupon data
         loadCouponData()


         // Initialize CouponsAdapter
         initializeCouponsAdapter()

         // Check if a new total price has been passed from CouponsActivity
        totalPrice = intent.getDoubleExtra("TOTAL_PRICE", 0.0)

        // Initialize views and RecyclerView
        rvCartDishes = findViewById(R.id.rvCartDishes)

        dishesList = mutableListOf()
        dishAdapter = DishAdapter(dishesList, RestaurantMenu())
        totalPriceCart = TotalPriceCart(dishesList, this, dishAdapter)
        rvCartDishes.layoutManager = LinearLayoutManager(this)
        rvCartDishes.adapter = totalPriceCart

        // Update total price if received from CouponsActivity
        if (totalPrice > 0.0) {
            updateTotalPrice()
            loadCartItems()
        } else {
            loadCartItems() // Load existing cart items if no total price is passed
        }

        saveCartItems()
        calculateTotalPrice()
        setRestaurantName()

        binding.backCart.setOnClickListener {
            finish()
        }

        binding.mcvSeleteAddress.setOnClickListener {
            notification()
        }

         binding.rlRestaurantcoupons.setOnClickListener {
             val intent = Intent(this@CartActivity, CouponsActivity::class.java)
             intent.putExtra("RESTAURANT_ID", restaurantId)

             // Pass the applied coupon value back to CouponsActivity
             intent.putExtra("APPLIED_COUPON_VALUE", appliedCouponValue)

             // Pass the total price to CouponsActivity
             intent.putExtra("TOTAL_PRICE", totalPrice)

             couponResultLauncher.launch(intent)
         }


         binding.removeCouponTextView.setOnClickListener {
            removeCoupon()

        }
    }


    private fun updateTotalPrice() {
        // Ensure the total price is correctly formatted
        tvTotalPrice.text = "Total Price: ₹$totalPrice"

        // Check if the total price is less than or equal to zero




    }

    private fun loadCartItems() {
        val sharedPreferences = getSharedPreferences("CartPreferences", MODE_PRIVATE)
        fetchRestaurantName(restaurantId)

        val cartItemsJson = sharedPreferences.getString("cart_items_$restaurantId", null)

        if (!cartItemsJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<Dish>>() {}.type
            val savedItems: List<Dish> = Gson().fromJson(cartItemsJson, type)

            dishesList.addAll(savedItems)

            totalPriceCart.notifyDataSetChanged()
            dishAdapter.notifyDataSetChanged()
        }
    }

    fun calculateTotalPrice() {
        // Calculate the total price of all dishes in the cart
        totalPrice = dishesList.sumByDouble { it.price.toDouble() * it.quantity }

        // Apply any coupon discount to the total price
        totalPrice -= appliedCouponValue

        // Update the total price TextView
        tvTotalPrice.text = "Total Price: ₹$totalPrice"

        val totalItems = dishesList.sumBy { it.quantity }
        if (totalItems == 0) {
            finish()
        }

        if (totalPrice <= 0) {
            Toast.makeText(this, "Total price is zero or less. Coupon will be unapplied.", Toast.LENGTH_SHORT).show()
            removeCoupon()
            return
        }
    }

    private fun setRestaurantName() {
        val restaurantName = intent.getStringExtra("RESTAURANT_NAME") ?: "Unknown Restaurant"
        Log.d("CartActivity", "Displaying restaurant name: $restaurantName")
        binding.tvCartNameRestauranrt.text = restaurantName
    }

    private fun saveCartItems() {
        val sharedPreferences = getSharedPreferences("CartPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val cartItemsJson = Gson().toJson(dishesList.filter { it.quantity > 0 })
        Log.d("CartActivity", "Saving JSON: $cartItemsJson")

        editor.putString("cart_items_$restaurantId", cartItemsJson)
        editor.putString("total_price_$restaurantId", totalPrice.toString())

        editor.apply()

        val savedJson = sharedPreferences.getString("cart_items_$restaurantId", null)
        Log.d("CartActivity", "Saved JSON in SharedPreferences: $savedJson")
    }

    private fun fetchRestaurantName(restaurantId: String) {
        val db = FirebaseFirestore.getInstance()
        val restaurantDocRef = db.collection("restaurants").document(restaurantId)

        restaurantDocRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val restaurantName = document.getString("name")
                binding.tvCartNameRestauranrt.text = restaurantName
            }
        }
    }

    private fun notification() {
        val notificationLayout = RemoteViews(packageName, R.layout.custom_notification)

        notificationLayout.setTextViewText(R.id.notification_title, "Order Confirmed!")
        notificationLayout.setTextViewText(R.id.notification_text, "Your order has been confirmed.")
        notificationLayout.setTextViewText(R.id.notification_details, "Expected delivery: 30 mins")

        val channelId = "order_confirmation_channel"
        val channelName = "Order Confirmation"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.zomatoicon)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(2, notificationBuilder.build())

        Toast.makeText(this, "Order Confirmed! A notification has been sent.", Toast.LENGTH_SHORT).show()
        showOrderConfirmationDialog()
    }

    private fun showOrderConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Order Confirmation")
        builder.setMessage("Your order has been confirmed. Would you like to view your order details?")

        builder.setPositiveButton("View Details") { dialog, which ->
            Toast.makeText(applicationContext, "Viewing Order Details...", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Close") { dialog, which ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottam_address, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()

        val someButton = bottomSheetView.findViewById<LinearLayout>(R.id.lladdesstv)
        someButton.setOnClickListener {
            // Implement action on click
        }
    }


    private fun removeCoupon() {
        // Add back the discount amount to the total price
        totalPrice += appliedCouponValue
        appliedCouponValue = 0.0
        selectedCouponId = null // Clear the selected coupon ID
        findViewById<RelativeLayout>(R.id.rlapplyedcoupon).visibility = View.GONE

        // Clear any saved coupon data
        clearCouponDataForRestaurant(restaurantId)

        // Notify the adapter if needed
        if (::couponsAdapter.isInitialized) {
            couponsAdapter.removeAppliedCoupon()
        }

        updateTotalPrice() // Update the total price after removing the coupon
    }



    private fun saveCouponData(value: Double, name: String) {
        val sharedPreferences = getSharedPreferences("CouponPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Save coupon data with a unique key per restaurant
        editor.putString("coupon_name_$restaurantId", name)
        editor.putString("coupon_value_$restaurantId", value.toString())
        editor.apply()
    }


    private fun loadCouponData() {
        val sharedPreferences = getSharedPreferences("CouponPrefs", MODE_PRIVATE)

        // Load coupon data for the specific restaurant
        val couponName = sharedPreferences.getString("coupon_name_$restaurantId", null)
        val couponValue = sharedPreferences.getString("coupon_value_$restaurantId", "0.0")?.toDouble() ?: 0.0

        if (couponName != null && couponValue > 0) {
            appliedCouponValue = couponValue
            binding.vResturentCupon.visibility =View.VISIBLE

            findViewById<RelativeLayout>(R.id.rlapplyedcoupon).visibility = View.VISIBLE

            val savedAmountText = "You saved ₹$appliedCouponValue with '$couponName'"
            tvSavedAmount.text = savedAmountText
            updateTotalPrice()
        }
    }


    private fun clearCouponDataForRestaurant(restaurantId: String) {
        val sharedPreferences = getSharedPreferences("CouponPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Remove coupon data for the specific restaurant cart
        editor.remove("coupon_name_$restaurantId")
        editor.remove("coupon_value_$restaurantId")
        editor.apply()
    }
    private fun initializeCouponsAdapter() {
        val coupons = loadCouponsFromFirestore() // Fetch the list of coupons from Firestore or another source
        couponsAdapter = CouponsAdapter(
            this,
            restaurantId,
            coupons,

            onCouponClicked = { coupon ->
                // Handle coupon click event
                appliedCouponValue = coupon.off.toDouble()
                saveCouponData(appliedCouponValue, coupon.details)
                updateTotalPrice()
                Toast.makeText(this, "Coupon ${coupon.details} applied!", Toast.LENGTH_SHORT).show()
            }
        )
    }
    private fun loadCouponsFromFirestore(): List<Coupon> {
        // Load the list of coupons from Firestore or another source
        // Dummy implementation
        return listOf(
            Coupon("10% OFF", "All items", "Discount on all items", "GETUDIT"),
            Coupon("20% OFF", "Beverages", "Discount on beverages only", "NEWUDIT")
        )
    }
}

