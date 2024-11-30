package com.example.pet_universe.database

import kotlinx.coroutines.flow.Flow

class RatingRepository(private val ratingDao: RatingDao) {
    suspend fun insert(rating: Rating) {
        ratingDao.insert(rating)
    }

    fun getRatingsForUser(userId: String): Flow<List<Rating>> {
        return ratingDao.getRatingsForUser(userId)
    }

    suspend fun getAverageRatingForUser(userId: String): Float? {
        return ratingDao.getAverageRatingForUser(userId)
    }
}
