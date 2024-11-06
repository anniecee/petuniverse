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

    @Query("SELECT * FROM listing_table WHERE seller_id = :userId")
    fun getListingsBySellerId(userId: Long): Flow<List<Listing>>

    @Query("SELECT * FROM listing_table WHERE seller_id = :userId AND is_sold = 0")
    fun getActiveListingsBySellerId(userId: Long): Flow<List<Listing>>
}
