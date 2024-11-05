package com.example.pet_universe.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDatabaseDao {
    @Insert
    suspend fun insert(listing: Listing)

    @Query("SELECT * FROM listing_table")
    fun getAll(): Flow<List<Listing>>

    @Query("DELETE FROM listing_table")
    suspend fun deleteAll()

    @Query("DELETE FROM listing_table WHERE id = :key")
    suspend fun delete(key: Long)
}
