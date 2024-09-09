package com.uditpatidar.zomatoapp.Adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uditpatidar.zomatoapp.R
import com.uditpatidar.zomatoapp.ui.Dataclass.Coupon

class CouponsAdapter(
    private val context: Context,
    private val restaurantId: String, // Pass restaurantId to save coupon specifically for a restaurant
    private val coupons: List<Coupon>,
    private val onCouponClicked: (Coupon) -> Unit // Callback to handle coupon click
) : RecyclerView.Adapter<CouponsAdapter.CouponViewHolder>() {

    private var selectedCouponPosition: Int = -1 // To track the selected coupon position
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("CouponPrefs", Context.MODE_PRIVATE)

    init {
        // Load the saved applied coupon position from SharedPreferences
        selectedCouponPosition = sharedPreferences.getInt("selectedCouponPosition_$restaurantId", -1)
    }

    // ViewHolder class to hold the view for each item
    inner class CouponViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOff: TextView = itemView.findViewById(R.id.tvOFF)
        private val tvItem: TextView = itemView.findViewById(R.id.item)
        private val tvDetails: TextView = itemView.findViewById(R.id.tvCouponCode)
        private val tapToApply: TextView = itemView.findViewById(R.id.tapToApply)

        // Bind data to the views
        fun bind(coupon: Coupon, position: Int) {
            val discountText = "Flat ₹${coupon.off} OFF"

            tvOff.text = discountText
            tvItem.text = coupon.item
            tvDetails.text = coupon.details

            // Check if the current position is the selected one
            if (position == selectedCouponPosition) {
                tapToApply.text = "Applied Coupon"
                tapToApply.setTextColor(itemView.context.getColor(R.color.green))
            } else {
                tapToApply.text = "Tap to apply"
                tapToApply.setTextColor(itemView.context.getColor(R.color.blue))
            }

            // Set click listener for the item
            itemView.setOnClickListener {
                // Update the selected coupon position
                selectedCouponPosition = position
                saveAppliedCouponDetails(coupon) // Save coupon details to SharedPreferences
                onCouponClicked(coupon)

                // Notify adapter to refresh the views
                notifyDataSetChanged()



            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_coupons, parent, false)
        return CouponViewHolder(view)
    }

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        holder.bind(coupons[position], position)
    }

    override fun getItemCount(): Int = coupons.size

    // Save each coupon's details uniformly in SharedPreferences using a unique key
    private fun saveAppliedCouponDetails(coupon: Coupon) {
        with(sharedPreferences.edit()) {
            val couponKey = "coupon_${restaurantId}_${coupon.details}" // Create a unique key with restaurantId
            putString("${couponKey}_off", coupon.off) // Save the coupon's off price
            putString("${couponKey}_details", coupon.details) // Save the coupon's details
            putInt("selectedCouponPosition_$restaurantId", selectedCouponPosition) // Save the selected position
            apply()
        }
    }
    fun removeAppliedCoupon() {
        with(sharedPreferences.edit()) {
            remove("selectedCouponPosition_$restaurantId")
            apply()
        }
        selectedCouponPosition = -1
        notifyDataSetChanged()
    }

}
