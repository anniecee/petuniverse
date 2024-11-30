package com.example.pet_universe.ui.buyView

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.pet_universe.R
import com.example.pet_universe.database.Listing
import com.example.pet_universe.ui.sellerView.SellerListingsAdapter.ViewHolder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesAdapter(private val requireContext: Context, private val favoritesList: MutableList<Listing>,
                       private val firestore: FirebaseFirestore) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {
    var onItemClick: ((Listing) -> Unit)? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val listingTitle: TextView = itemView.findViewById(R.id.listingTitle)
        val listingPrice: TextView = itemView.findViewById(R.id.listingPrice)
        val listingDescription: TextView = itemView.findViewById(R.id.listingDescription)
        val listingCategory: TextView = itemView.findViewById(R.id.listingCategory)
        val listingPhoto: ImageView = itemView.findViewById(R.id.listingPhoto)

        // Function to set image for the listing
        fun bindData(listing: Listing) {
         if (listing.imageUrl != null) {
             val image = listing.imageUrl
             listingPhoto.load(image) {
                 crossfade(true)
                 placeholder(R.drawable.image_placeholder)
                 error(R.drawable.image_placeholder)
             }
         }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = View.inflate(parent.context, R.layout.favorite_listings, null)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return favoritesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        println("Debug: position: $position")
        holder.listingTitle.text = favoritesList[position].title
        holder.listingPrice.text = "$" + favoritesList[position].price.toString()
        holder.listingCategory.text = "Listed In: ${favoritesList[position].category}, ${favoritesList[position].type}"

        val maxLength = 80
        val cleanedDescription = favoritesList[position].description.replace("\n", " ")
        val truncatedDescription = if (cleanedDescription.length > maxLength) {
            cleanedDescription.take(maxLength) + "..."
        } else {
            cleanedDescription
        }
        holder.listingDescription.text = truncatedDescription

        // Set image of listing
        holder.bindData(favoritesList[position])

        // Set up onClickListener for the item
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(favoritesList[position])
        }
    }

}
