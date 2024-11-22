package com.example.pet_universe.ui.sellerView

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.pet_universe.R
import com.example.pet_universe.database.Listing
import com.example.pet_universe.database.ListingDatabase
import com.example.pet_universe.database.ListingDatabaseDao
import com.example.pet_universe.database.ListingRepository
import com.example.pet_universe.database.ListingViewModel
import com.example.pet_universe.database.ListingViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import coil.load

class SellerListingsAdapter(private val context: Context, private val sellerListings: MutableList<Listing>, private val firestore: FirebaseFirestore,
                            private val sharedPref: SharedPreferences
) :
    RecyclerView.Adapter<SellerListingsAdapter.ViewHolder>() {
    // Database
    private lateinit var database: ListingDatabase
    private lateinit var listingDao: ListingDatabaseDao
    private lateinit var repository: ListingRepository
    private lateinit var viewModelFactory: ListingViewModelFactory
    private lateinit var listingViewModel: ListingViewModel

    var onItemClick: ((Listing) -> Unit)? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val listingTitle: TextView = itemView.findViewById(R.id.listingTitle)
        val listingPrice: TextView = itemView.findViewById(R.id.listingPrice)
        val listingDescription: TextView = itemView.findViewById(R.id.listingDescription)
        val listingCategory: TextView = itemView.findViewById(R.id.listingCategory)
        val listingPhoto: ImageView = itemView.findViewById(R.id.listingPhoto)
        val deleteButton: FloatingActionButton = itemView.findViewById(R.id.deleteButton)

        // Function to set image for the listing
        fun bindData(listing: Listing) {
            if (listing.imageUrl != null) {
                val image = listing.imageUrl
                listingPhoto.load(image) {
                    crossfade(true)
                }
            }
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

        holder.deleteButton.setOnClickListener {
            val listingTitle = sellerListings[position].title

            // Show confirmation dialog
            AlertDialog.Builder(context)
                .setTitle("Delete $listingTitle")
                .setMessage("Are you sure you want to delete this listing?")
                .setPositiveButton("Yes") { _, _ ->
                    // User confirmed deletion
                    removeListing(position)
                    sellerListings.removeAt(position)
                    notifyItemRemoved(position)
                }
                .setNegativeButton("No") { dialog, _ ->
                    // User canceled deletion
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun removeListing(position: Int) {
        // Set up database
        database = ListingDatabase.getInstance(context)
        listingDao = database.listingDao
        repository = ListingRepository(listingDao)
        viewModelFactory = ListingViewModelFactory(repository)
        listingViewModel =
            ViewModelProvider(context as ViewModelStoreOwner, viewModelFactory).get(
                ListingViewModel::class.java
            )

        // Remove the listing from the database
        listingViewModel.delete(sellerListings[position].id)

        // Remove the listing from Firebase
        deleteListingFromFirebase(sellerListings[position].id)

        Toast.makeText(context, "Listing deleted", Toast.LENGTH_SHORT).show()
    }

    // Remove listing from Firebase in both the user's collection and the global collection
    private fun deleteListingFromFirebase(listingId: Long) {
        val userId = sharedPref.getString("userId", null) ?: return
        val userListingRef = firestore.collection("users").document(userId).collection("listings")
            .document(listingId.toString())
        val globalListingRef = firestore.collection("listings").document(listingId.toString())

        userListingRef.delete()
            .addOnSuccessListener {
                println( "Listing deleted in user's collection")
            }
            .addOnFailureListener { e ->
                println("Failed to delete listing: ${e.message}")
            }

        globalListingRef.delete()
            .addOnSuccessListener {
                println("Listing deleted in global collection")
            }
            .addOnFailureListener { e ->
                println("Failed to delete listing: ${e.message}")
            }
    }

}