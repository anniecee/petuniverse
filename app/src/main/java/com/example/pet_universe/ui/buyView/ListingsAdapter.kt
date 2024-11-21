package com.example.pet_universe.ui.buyView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.pet_universe.R
import com.example.pet_universe.database.Listing

class ListingsAdapter(
    private var listingsList: List<Listing>,
    private val onItemClick: (Listing) -> Unit
) : RecyclerView.Adapter<ListingsAdapter.ListingViewHolder>() {

    inner class ListingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val listingImageView: ImageView = itemView.findViewById(R.id.listingImageView)
        val listingTitleTextView: TextView = itemView.findViewById(R.id.listingTitleTextView)
        val listingPriceTextView: TextView = itemView.findViewById(R.id.listingPriceTextView)
        val listingTypeTextView: TextView = itemView.findViewById(R.id.listingTypeTextView)
        val listingDescriptionTextView: TextView =
            itemView.findViewById(R.id.listingDescriptionTextView)

        init {
            itemView.setOnClickListener {
                onItemClick(listingsList[adapterPosition])
            }
        }

        fun bindData(listing: Listing) {
            if (listing.imageUrls.isNotEmpty()) {
                val imageUrl = listing.imageUrls[0]
                listingImageView.load(imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.pet_accessories_logo)
                    error(R.drawable.pet_accessories_logo)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_in_listings_list, parent, false)
        return ListingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        val listing = listingsList[position]
        holder.bindData(listing)
        holder.listingTitleTextView.text = listing.title
        holder.listingPriceTextView.text = "$${listing.price}"
        holder.listingTypeTextView.text = listing.type

        val maxLength = 80
        val cleanedDescription = listing.description.replace("\n", " ")
        val truncatedDescription = if (cleanedDescription.length > maxLength) {
            cleanedDescription.take(maxLength) + "..."
        } else {
            cleanedDescription
        }
        holder.listingDescriptionTextView.text = truncatedDescription
    }

    override fun getItemCount(): Int = listingsList.size

    fun updateListings(newListings: List<Listing>) {
        listingsList = newListings
        notifyDataSetChanged()
    }
}