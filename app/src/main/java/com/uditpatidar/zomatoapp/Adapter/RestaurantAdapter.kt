


import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.uditpatidar.zomatoapp.R
import com.uditpatidar.zomatoapp.ui.Activity.RestaurantMenu
class RestaurantAdapter(
    private val context: Context,
    private val restaurants: List<Restaurant>,
    private val onRestaurantClick: (Restaurant) -> Unit) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {


    inner class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.restaurant_image)
        val nameTextView: TextView = itemView.findViewById(R.id.restaurant_name)
        val locationTextView: TextView = itemView.findViewById(R.id.restaurant_location)
        val rating :TextView = itemView.findViewById(R.id.rating)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val restaurant = restaurants[position]
                    Log.d("RestaurantAdapter", "Clicked on restaurant: ${restaurant.name}, ID: ${restaurant.id}")
                    onRestaurantClick(restaurant)

                }
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]
       // holder.bind(restaurants[position])
        holder.nameTextView.text = restaurant.name
        holder.locationTextView.text = restaurant.location

   /* holder.itemView.setOnClickListener {
            onRestaurantClick(restaurant)
        }*/

        Glide.with(holder.itemView.context)
            .load(restaurant.imageUrl)
            .placeholder(R.drawable.biryani)
            .error(R.drawable.error)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = restaurants.size


}

data class Restaurant(
    val name: String = "",
    val imageUrl: String = "",
    val location: String = "",
    val id: String = "",
    val totalQuantity:String=" "

)