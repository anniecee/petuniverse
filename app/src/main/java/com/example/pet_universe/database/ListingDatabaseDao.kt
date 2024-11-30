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
    fun getListingById(listingId: Long): Listing?

    @Query("UPDATE listing_table SET title = :title, price = :price, description = :description, meeting_location = :meetingLocation, category = :category, imageUrl = :imageUrl, type = :type, is_fav = :isFav WHERE id = :id")
    suspend fun updateListing(id: Long, title: String, price: Int, description: String, meetingLocation: String, category: String, imageUrl: String, type: String, isFav: Boolean)

    @Query("SELECT * FROM listing_table")
    fun getAllListings(): Flow<List<Listing>>

    suspend fun update(listing: Listing) {
        println("Executing update query: UPDATE listing_table SET title = '${listing.title}', price = ${listing.price}, description = '${listing.description}', meeting_location = '${listing.meetingLocation}', category = '${listing.category}', imageUrls = '${listing.imageUrl}', type = '${listing.type}', isFav = '${listing.isFav}' WHERE id = ${listing.id}")
        updateListing(listing.id, listing.title, listing.price, listing.description, listing.meetingLocation, listing.category, listing.imageUrl, listing.type, listing.isFav)
    }

    @Query ("SELECT * FROM listing_table WHERE is_fav = 1")
    fun getFavoritesListings(): Flow<List<Listing>>
}
