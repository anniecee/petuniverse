package com.example.pet_universe.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListingViewModel(private val repository: ListingRepository): ViewModel() {
    val allListingsLiveData: LiveData<List<Listing>> = repository.allListings.asLiveData()
    // Firebase Firestore instance
    private val firestore = FirebaseFirestore.getInstance()

    fun insert(listing: Listing) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(listing)
        }
    }

    fun insertListings(listings: List<Listing>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertListings(listings)
        }
    }

    // Fetch listings from Firebase and save to Room
    fun fetchListingsFromFirebase() {
        firestore.collection("listings")
            .get()
            .addOnSuccessListener { result ->
                val listings = result.map { document ->
                    document.toObject(Listing::class.java)
                }
                saveListingsToLocalDatabase(listings)
            }
            .addOnFailureListener { e ->
                 println("got the error in the fetchListingsFromFirebase function in ListingViewModel. $e")
            }
    }

    // Save fetched listings to Room database
    private fun saveListingsToLocalDatabase(listings: List<Listing>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertListings(listings)
        }
    }


    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(id)
        }
    }

    suspend fun getListingsBySellerId(userId: Long): LiveData<List<Listing>> {
        return withContext(Dispatchers.IO) {
            repository.getListingsBySellerId(userId).asLiveData()
        }
    }

    suspend fun getActiveListingsBySellerId(userId: String): LiveData<List<Listing>> {
        return withContext(Dispatchers.IO) {
            repository.getActiveListingsBySellerId(userId).asLiveData()
        }
    }

    suspend fun getListingById(listingId: Long): Listing? {
        return withContext(Dispatchers.IO) {
            repository.getListingById(listingId)
        }
    }

    fun updateListing(listing: Listing) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = listing.id
            val title = listing.title
            val price = listing.price
            val description = listing.description
            val category = listing.category
            val photo = listing.imageUrls
            val meetingLocation = listing.meetingLocation

            repository.updateListing(id, title, price, description, meetingLocation, category, photo)
        }
    }



}

class ListingViewModelFactory(private val repository: ListingRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
