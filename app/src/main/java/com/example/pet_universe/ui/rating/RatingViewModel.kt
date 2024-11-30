// RatingViewModel.kt
package com.example.pet_universe.ui.rating

import androidx.lifecycle.*
import com.example.pet_universe.database.Rating
import com.example.pet_universe.database.RatingRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RatingViewModel(private val ratingRepository: RatingRepository) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _averageRating = MutableLiveData<Float>()
    val averageRating: LiveData<Float> get() = _averageRating

//    fun submitRating(rating: Rating) {
//        viewModelScope.launch(Dispatchers.IO) {
//            // Generate a new rating ID
//            val ratingRef = firestore.collection("ratings").document()
//            val ratingId = ratingRef.id
//            val ratingWithId = rating.copy(id = ratingId)
//
//            // Save to Firebase
//            ratingRef.set(ratingWithId.toMap())
//                .addOnSuccessListener {
//                    // Insert into local database
//                    viewModelScope.launch(Dispatchers.IO) {
//                        ratingRepository.insert(ratingWithId)
//                    }
//                }
//                .addOnFailureListener { e ->
//                    println("Error submitting rating to Firebase: $e")
//                }
//        }
//    }

    fun submitRating(rating: Rating) {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if rating already exists
            val existingRating = ratingRepository.getRating(rating.fromUserId, rating.toUserId, rating.listingId)
            if (existingRating != null) {
                // Update existing rating in Firebase
                firestore.collection("ratings").document(existingRating.id)
                    .set(rating.toMap())
                    .addOnSuccessListener {
                        // Update local database
                        viewModelScope.launch(Dispatchers.IO) {
                            ratingRepository.insert(rating.copy(id = existingRating.id))
                        }
                    }
                    .addOnFailureListener { e ->
                        println("Error updating rating in Firebase: $e")
                    }
            } else {
                // Create new rating
                val ratingRef = firestore.collection("ratings").document()
                val ratingId = ratingRef.id
                val ratingWithId = rating.copy(id = ratingId)

                // Save to Firebase
                ratingRef.set(ratingWithId.toMap())
                    .addOnSuccessListener {
                        // Insert into local database
                        viewModelScope.launch(Dispatchers.IO) {
                            ratingRepository.insert(ratingWithId)
                        }
                    }
                    .addOnFailureListener { e ->
                        println("Error submitting rating to Firebase: $e")
                    }
            }
        }
    }

    fun fetchAverageRating(toUserId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Fetch ratings from Firebase
            val ratingsSnapshot = firestore.collection("ratings")
                .whereEqualTo("toUserId", toUserId)
                .get()
                .await()

            val ratings = ratingsSnapshot.documents.mapNotNull { doc ->
                val rating = doc.toObject(Rating::class.java)
                rating?.copy(id = doc.id)
            }

            // Save ratings to local database
            ratings.forEach { rating ->
                ratingRepository.insert(rating)
            }

            // Calculate average rating
            val averageRating = ratingRepository.getAverageRatingForUser(toUserId) ?: 0f

            _averageRating.postValue(averageRating)
        }
    }

    suspend fun getAverageRating(toUserId: String): Float {
        // Fetch ratings from Firebase
        val ratingsSnapshot = firestore.collection("ratings")
            .whereEqualTo("toUserId", toUserId)
            .get()
            .await()

        val ratings = ratingsSnapshot.documents.mapNotNull { doc ->
            val rating = doc.toObject(Rating::class.java)
            rating?.copy(id = doc.id)
        }

        // Save ratings to local database
        ratings.forEach { rating ->
            ratingRepository.insert(rating)
        }

        // Calculate average rating
        return ratingRepository.getAverageRatingForUser(toUserId) ?: 0f
    }

    // Function to check if the user has already rated the seller for this listing
    suspend fun hasUserRatedListing(fromUserId: String, toUserId: String, listingId: Long): Boolean {
        // Check in local database first
        val existingRating = ratingRepository.getRating(fromUserId, toUserId, listingId)
        if (existingRating != null) {
            return true
        }

        // If not found locally, check in Firebase
        val ratingsSnapshot = firestore.collection("ratings")
            .whereEqualTo("fromUserId", fromUserId)
            .whereEqualTo("toUserId", toUserId)
            .whereEqualTo("listingId", listingId)
            .get()
            .await()

        val ratings = ratingsSnapshot.documents.mapNotNull { doc ->
            val rating = doc.toObject(Rating::class.java)
            rating?.copy(id = doc.id)
        }

        // Save fetched ratings to local database
        ratings.forEach { rating ->
            ratingRepository.insert(rating)
        }

        return ratings.isNotEmpty()
    }
}


class RatingViewModelFactory(private val ratingRepository: RatingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RatingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RatingViewModel(ratingRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
