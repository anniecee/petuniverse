package com.example.pet_universe.ui.sellerView

import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pet_universe.R
import com.example.pet_universe.database.Listing

class SellerListingsAdapter(private val sellerListings : MutableList<Listing>) :
    RecyclerView.Adapter<SellerListingsAdapter.ViewHolder>() {
        var onItemClick: ((Listing) -> Unit)? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val listingTitle: TextView = itemView.findViewById(R.id.listingTitle)
        val listingPrice: TextView = itemView.findViewById(R.id.listingPrice)
        val listingDescription: TextView = itemView.findViewById(R.id.listingDescription)
        val listingCategory: TextView = itemView.findViewById(R.id.listingCategory)
        val listingPhoto: ImageView = itemView.findViewById(R.id.listingPhoto)

        // This function is called to set the image of the listing
        fun bindData(listing: Listing) {
            val bitmap = BitmapFactory.decodeByteArray(listing.photo, 0, listing.photo.size)
            listingPhoto.setImageBitmap(bitmap)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = View.inflate(parent.context, R.layout.seller_listings, null)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sellerListings.size
    }

    // This function is called when the RecyclerView needs to display data at a certain position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.listingTitle.text = sellerListings[position].title
        holder.listingPrice.text = "$" + sellerListings[position].price.toString()
        holder.listingDescription.text = sellerListings[position].description
        holder.listingCategory.text = "Category: " + sellerListings[position].category

        // Set image of listing
        holder.bindData(sellerListings[position])

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(sellerListings[position])
        }
    }

}
