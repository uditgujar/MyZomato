package com.uditpatidar.zomatoapp.ui.Dataclass

data class Coupon(
    var id:String,
    val off: String ,
    val item: String ,
    val details: String ,
    var isApplied: Boolean = false // Ensure this is mutable
)