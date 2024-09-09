package com.uditpatidar.zomatoapp.ui.Fragment


import Restaurant
import RestaurantAdapter
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator
import com.uditpatidar.zomatoapp.Adapter.PosterAdapter
import com.uditpatidar.zomatoapp.Adapter.adapterphoto
import com.uditpatidar.zomatoapp.R
import com.uditpatidar.zomatoapp.databinding.FragmentHomeBinding
import com.uditpatidar.zomatoapp.ui.Dataclass.dataposet
import com.uditpatidar.zomatoapp.ui.Dataclass.dishesDataClass
import com.google.firebase.firestore.FirebaseFirestore
import com.uditpatidar.zomatoapp.ui.Activity.RestaurantMenu

class HomeFragment : Fragment() {

    private lateinit var adapter1: adapterphoto
    private lateinit var restaurantAdapter: RestaurantAdapter
    private lateinit var pagerAdapter: PosterAdapter
    private val handler = Handler()
    private lateinit var viewPager: ViewPager2
    private lateinit var firestore: FirebaseFirestore


    private val array1 = ArrayList<dataposet>()
    private val array = ArrayList<dishesDataClass>()
    private lateinit var restaurantList: MutableList<Restaurant>

    private val sliderRunnable = Runnable {
        if (viewPager.currentItem == array1.size - 1) {
            viewPager.currentItem = 0
        } else {
            viewPager.currentItem = viewPager.currentItem + 1
        }
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeData()



        val rc1: RecyclerView = binding.rvposter
        val rc3: RecyclerView = binding.rvms
        viewPager = binding.viewPager
        val springDotsIndicator: SpringDotsIndicator = binding.dotsIndicator

        // Set up first RecyclerView
        adapter1 = adapterphoto(requireContext(), array)
        rc1.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        rc1.adapter = adapter1

        // Set up second RecyclerView
        firestore = FirebaseFirestore.getInstance()
        restaurantList = mutableListOf()
        restaurantAdapter = RestaurantAdapter(requireContext(), restaurantList) { restaurant ->
            Log.d(TAG, "Clicked restaurant ID: ${restaurant.id}")
            val intent = Intent(requireContext(), RestaurantMenu::class.java)
            intent.putExtra("RESTAURANT_ID", restaurant.id)
            intent.putExtra("RESTAURANT_NAME", restaurant.name)
            startActivity(intent)
        }
        rc3.layoutManager = LinearLayoutManager(requireContext())
        rc3.adapter = restaurantAdapter



        // Fetch data from Firestore
        fetchRestaurantData()

        // Set up ViewPager
        pagerAdapter = PosterAdapter(requireContext(), array1, viewPager)
        viewPager.adapter = pagerAdapter
        viewPager.clipToPadding = false
        viewPager.clipChildren = false
        viewPager.offscreenPageLimit = 3

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.postDelayed(sliderRunnable, 3000)
            }
        })

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(40))
        springDotsIndicator.attachTo(viewPager)
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }

        viewPager.setPageTransformer(compositePageTransformer)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(sliderRunnable)
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(sliderRunnable, 3000)
    }

    private fun initializeData() {
        array1.clear()
        array.clear()

        array1.add(dataposet(R.drawable.hotel1))
        array1.add(dataposet(R.drawable.hotel2))
        array1.add(dataposet(R.drawable.hotel3))
        array1.add(dataposet(R.drawable.hotel4))

        array.add(dishesDataClass(R.drawable.helthy, "Healthy"))
        array.add(dishesDataClass(R.drawable.dishes, "Pizza"))
        array.add(dishesDataClass(R.drawable.dishes2, "Home style"))
        array.add(dishesDataClass(R.drawable.dishes3, "Chicken"))
        array.add(dishesDataClass(R.drawable.helthy, "Healthy"))
        array.add(dishesDataClass(R.drawable.dishes, "Pizza"))
        array.add(dishesDataClass(R.drawable.dishes2, "Home style"))
        array.add(dishesDataClass(R.drawable.dishes3, "Chicken"))
    }

    private fun fetchRestaurantData() {
        firestore.collection("restaurants") // Update with your collection name
            .get()
            .addOnSuccessListener { result ->
                restaurantList.clear()
                for (document in result) {
                    val name = document.getString("name") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val location = document.getString("location") ?: ""
                    val rating = document.getString("rating")
                    val id = document.id // Use the Firestore document ID as the restaurant ID

                    val restaurant = Restaurant(name, imageUrl, location,id) // Include the ID
                    restaurantList.add(restaurant)
                }
                restaurantAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "fetchRestaurantData: ${exception.message}")
                // Handle error
            }
    }
}
