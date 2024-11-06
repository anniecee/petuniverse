package com.example.pet_universe.ui.sellerView

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pet_universe.R
import com.example.pet_universe.database.Listing

class SellerListingsAdapter(private val sellerListings : MutableList<Listing>) :
    RecyclerView.Adapter<SellerListingsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // TODO: Add more fields as needed
        val listingTitle: TextView = itemView.findViewById(R.id.listingTitle)
        val listingPrice: TextView = itemView.findViewById(R.id.listingPrice)
        val listingDescription: TextView = itemView.findViewById(R.id.listingDescription)
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
        // TODO: Add more fields as needed
        holder.listingTitle.text = sellerListings[position].title
        holder.listingPrice.text = "$" + sellerListings[position].price.toString()
        holder.listingDescription.text = sellerListings[position].description
    }

}
