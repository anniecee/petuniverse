package com.example.pet_universe.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDatabaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(listing: Listing)

    // Bulk insert for multiple listings
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(listings: List<Listing>)

    @Query("SELECT * FROM listing_table")
    fun getAll(): Flow<List<Listing>>

    @Query("DELETE FROM listing_table")
    suspend fun deleteAll()

    @Query("DELETE FROM listing_table WHERE id = :key")
    suspend fun delete(key: Long)

    @Query("SELECT * FROM listing_table WHERE seller_id = :userId")
    fun getListingsBySellerId(userId: Long): Flow<List<Listing>>

    @Query("SELECT * FROM listing_table WHERE seller_id = :userId AND is_sold = 0")
    fun getActiveListingsBySellerId(userId: String): Flow<List<Listing>>

    @Query("SELECT * FROM listing_table WHERE id = :listingId LIMIT 1")
    fun getListingById(listingId: Long): Listing

    @Query("UPDATE listing_table SET title = :title, price = :price, description = :description, meeting_location = :meetingLocation, category = :category, photo = :photo WHERE id = :id")
    fun update(id: Long, title: String, price: Double, description: String, meetingLocation: String, category: String, photo: ByteArray?)
}
