package com.example.pet_universe.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListingRepository(private val listingDatabaseDao: ListingDatabaseDao) {
    val allListings: Flow<List<Listing>> = listingDatabaseDao.getAll()

    fun insert(listing: Listing) {
        CoroutineScope(IO).launch {
            listingDatabaseDao.insert(listing)
        }
    }

    // Insert multiple listings (bulk insert)
    fun insertListings(listings: List<Listing>) {
        CoroutineScope(IO).launch {
            listingDatabaseDao.insertAll(listings)
        }
    }

    fun deleteAll() {
        CoroutineScope(IO).launch {
            listingDatabaseDao.deleteAll()
        }
    }

    fun delete(id: Long) {
        CoroutineScope(IO).launch {
            listingDatabaseDao.delete(id)
        }
    }

    suspend fun getListingsBySellerId(userId: Long): Flow<List<Listing>> {
        return withContext(IO) {
            listingDatabaseDao.getListingsBySellerId(userId)
        }
    }

    suspend fun getActiveListingsBySellerId(userId: String): Flow<List<Listing>>  {
        return withContext(IO){
            listingDatabaseDao.getActiveListingsBySellerId(userId)
        }

    }

    suspend fun getListingById(listingId: Long): Listing {
        return withContext(IO) {
            listingDatabaseDao.getListingById(listingId)
        }

    }

    fun updateListing(id: Long, title: String, price: Double, description: String, meetingLocation: String, category: String, photo: ByteArray) {
        CoroutineScope(IO).launch {
            listingDatabaseDao.update(id, title, price, description, meetingLocation, category, photo)
        }
    }

}
