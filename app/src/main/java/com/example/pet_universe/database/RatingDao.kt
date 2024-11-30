package com.example.pet_universe.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rating: Rating)

    @Query("SELECT * FROM rating_table WHERE toUserId = :userId")
    fun getRatingsForUser(userId: String): Flow<List<Rating>>

    @Query("SELECT AVG(ratingValue) FROM rating_table WHERE toUserId = :userId")
    suspend fun getAverageRatingForUser(userId: String): Float?
}
