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

    fun submitRating(rating: Rating) {
        viewModelScope.launch(Dispatchers.IO) {
            // Generate a new rating ID
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
