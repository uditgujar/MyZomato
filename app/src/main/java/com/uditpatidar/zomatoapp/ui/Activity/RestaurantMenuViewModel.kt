package com.uditpatidar.zomatoapp.ui.Activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uditpatidar.zomatoapp.Adapter.Dish

class RestaurantMenuViewModel : ViewModel() {
    val dishesList = MutableLiveData<MutableList<Dish>>()
    val totalItemsAdded = MutableLiveData<Int>()

    init {
        dishesList.value = mutableListOf()
        totalItemsAdded.value = 0
    }

    fun addItem() {
        totalItemsAdded.value = (totalItemsAdded.value ?: 0) + 1
    }

    fun removeItem() {
        val currentTotal = totalItemsAdded.value ?: 0
        if (currentTotal > 0) {
            totalItemsAdded.value = currentTotal - 1
        }
    }
}
