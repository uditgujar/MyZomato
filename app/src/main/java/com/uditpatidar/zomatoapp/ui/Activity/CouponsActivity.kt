package com.uditpatidar.zomatoapp.ui.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.uditpatidar.zomatoapp.Adapter.CouponsAdapter

import com.uditpatidar.zomatoapp.R
import com.uditpatidar.zomatoapp.databinding.ActivityCouponsBinding
import com.uditpatidar.zomatoapp.ui.Dataclass.Coupon


class CouponsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCouponsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var couponsAdapter: CouponsAdapter
    private val couponsList = mutableListOf<Coupon>()
    private var totalPrice: Double = 0.0
    lateinit var restaurantId: String
    private lateinit var firestore: FirebaseFirestore
    private var appliedCouponValue: Double = 0.0

    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("CouponPrefs", Context.MODE_PRIVATE)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCouponsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firestore = FirebaseFirestore.getInstance()
        appliedCouponValue = intent.getDoubleExtra("APPLIED_COUPON_VALUE", 0.0)
        // appliedCouponCode = intent.getDoubleExtra("APPLIED_COUPON_CODE","")
        restaurantId = intent.getStringExtra("RESTAURANT_ID") ?: ""

        totalPrice = intent.getDoubleExtra("TOTAL_PRICE", 0.0)

        recyclerView = findViewById(R.id.recyclerViewCoupons)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the couponsAdapter before setting it to the RecyclerView
        couponsAdapter = CouponsAdapter(this, restaurantId, couponsList) { coupon ->
            applyCoupon(coupon)
        }

        recyclerView.adapter = couponsAdapter


        if (restaurantId.isNotEmpty()) {
            fetchCouponsData()
        } else {
            Log.e("RestaurantMenuActivity", "Invalid restaurant ID")
        }



        binding.backCoupons.setOnClickListener {
            finish()
        }
    }

    private fun applyCoupon(coupon: Coupon) {
        val discountAmount = coupon.off.toDouble()
        val couponCode = coupon.details // Assuming coupon.item contains the coupon code

        totalPrice -= (totalPrice * discountAmount / 100)

        // Update coupon list and notify the adapter
        couponsList.forEach { it.isApplied = (it.id == coupon.id) }
        couponsAdapter.notifyDataSetChanged()

        val resultIntent = Intent()
        resultIntent.putExtra("UPDATED_TOTAL_PRICE", totalPrice)
        resultIntent.putExtra("APPLIED_COUPON_VALUE", discountAmount)
        resultIntent.putExtra(
            "APPLIED_COUPON_CODE",
            couponCode
        ) // Pass the coupon code to the intent
        setResult(Activity.RESULT_OK, resultIntent)

        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 2000)
    }


    private fun fetchCouponsData() {
        firestore.collection("restaurants")
            .document(restaurantId)
            .collection("coupons")
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    Log.e("Firestore", "Error fetching dishes: ", exception)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    couponsList.clear()  // Clear the existing list
                    for (document in snapshots.documents) {
                        val off = document.getString("off") ?: ""
                        val item = document.getString("item") ?: ""
                        val details = document.getString("details") ?: ""

                        // Convert the "off" value to a Double and filter based on totalPrice
                        val offValue = off.toDoubleOrNull() ?: 0.0
                        if (offValue <= totalPrice) {
                            couponsList.add(Coupon("", off, item, details))
                        }
                    }
                    // Notify the adapter about the data change
                    couponsAdapter.notifyDataSetChanged()
                }
            }
    }
}