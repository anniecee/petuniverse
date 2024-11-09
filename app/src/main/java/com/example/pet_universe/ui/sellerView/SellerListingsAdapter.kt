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
        listingViewModel = ViewModelProvider(context as ViewModelStoreOwner, viewModelFactory).get(ListingViewModel::class.java)

        // Remove the listing from the database
        listingViewModel.delete(sellerListings[position].id)
    }

//    private fun removeListing(position: Int) {
//        database = ListingDatabase.getInstance(context)
//        listingDao = database.listingDao
//        repository = ListingRepository(listingDao)
//        viewModelFactory = ListingViewModelFactory(repository)
//        listingViewModel = ViewModelProvider(context as ViewModelStoreOwner, viewModelFactory).get(ListingViewModel::class.java)
//        val listingId = sellerListings[position].id
//        sellerListings.removeAt(position)
//        notifyItemRemoved(position)
//        deleteListingFromFirebase(listingId)  // Remove from Firebase
//        listingViewModel.delete(listingId)  // Remove from Room database
//    }
//
//    private fun deleteListingFromFirebase(listingId: Long) {
//        val userId = sharedPref.getString("userId", null) ?: return
//        firestore.collection("users/$userId/listings")
//            .document(listingId.toString())
//            .delete()
//            .addOnSuccessListener {
//                Toast.makeText(context, "Listing deleted", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(context, "Failed to delete listing: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }

}
