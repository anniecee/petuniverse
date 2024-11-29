package com.example.pet_universe.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListingViewModel(private val repository: ListingRepository) : ViewModel() {
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
    private var isFetching = false

    fun fetchPetListingsFromFirebase() {
        if (isFetching) return
        isFetching = true

        firestore.collection("listings")
            .whereEqualTo("category", "Live Pets")
            .get()
            .addOnSuccessListener { result ->
                val listings = result.map { document ->
                    document.toObject(Listing::class.java)
                }
                println("Debug: Fetched ${listings.size} Pet Listings")
                saveListingsToLocalDatabase("Live Pets", listings)
            }
            .addOnFailureListener { e ->
                println("Error fetching Pet Listings: $e")
            }
            .addOnCompleteListener {
                isFetching = false
            }
    }

    fun fetchFoodListingsFromFirebase() {
        if (isFetching) return
        isFetching = true

        firestore.collection("listings")
            .whereEqualTo("category", "Pet Food")
            .get()
            .addOnSuccessListener { result ->
                val listings = result.map { document ->
                    document.toObject(Listing::class.java)
                }
                println("Debug: Fetched ${listings.size} Food Listings")
                saveListingsToLocalDatabase("Pet Food", listings)
            }
            .addOnFailureListener { e ->
                println("Error fetching Food Listings: $e")
            }
            .addOnCompleteListener {
                isFetching = false
            }
    }

    fun fetchAccessoryListingsFromFirebase() {
        if (isFetching) return
        isFetching = true

        firestore.collection("listings")
            .whereEqualTo("category", "Pet Accessories")
            .get()
            .addOnSuccessListener { result ->
                val listings = result.map { document ->
                    document.toObject(Listing::class.java)
                }
                println("Debug: Fetched ${listings.size} Accessory Listings")
                saveListingsToLocalDatabase("Pet Accessories", listings)
            }
            .addOnFailureListener { e ->
                println("Error fetching Accessory Listings: $e")
            }
            .addOnCompleteListener {
                isFetching = false
            }
    }

    fun fetchOtherListingsFromFirebase() {
        if (isFetching) return
        isFetching = true

        firestore.collection("listings")
            .whereEqualTo("category", "Other")
            .get()
            .addOnSuccessListener { result ->
                val listings = result.map { document ->
                    document.toObject(Listing::class.java)
                }
                println("Debug: Fetched ${listings.size} Other Listings")
                saveListingsToLocalDatabase("Other", listings)
            }
            .addOnFailureListener { e ->
                println("Error fetching Other Listings: $e")
            }
            .addOnCompleteListener {
                isFetching = false
            }
    }

    // Save fetched listings to Room database
    private fun saveListingsToLocalDatabase(category: String, firebaseListings: List<Listing>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Collect current listings from Flow
                val currentListings = repository.getAllListings().first()

                // Listings that already exist in local database for this category
                val existingListings = currentListings.filter { it.category == category }

                // Find the listings that need to be deleted (those in local db but not in firebase)
                val listingsToRemove = existingListings.filterNot { localListing ->
                    firebaseListings.any { firebaseListing -> firebaseListing.id == localListing.id }
                }

                // Find the listings to add (those in firebase but not in local db)
                val listingsToAdd = firebaseListings.filterNot { firebaseListing ->
                    existingListings.any { localListing -> localListing.id == firebaseListing.id }
                }

                // Delete listings that don't exist in firebase anymore
                listingsToRemove.forEach { repository.delete(it.id) }

                // Insert new listings that don't exist in local database
                if (listingsToAdd.isNotEmpty()) {
                    repository.insertListings(listingsToAdd)
                    println("Saved ${listingsToAdd.size} $category listings to local database")
                } else {
                    println("No new $category listings to save")
                }

                // Log current database state
//                val updatedListings = repository.getAllListings().first()
//                println("Total listings after update: ${updatedListings.size}")
//                println("Listings in $category: ${updatedListings.filter { it.category == category }.size}")
            } catch (e: Exception) {
                println("Error saving $category listings to local database: $e")
            }
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
            repository.update(listing)
        }
    }
}

class ListingViewModelFactory(private val repository: ListingRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return ListingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

