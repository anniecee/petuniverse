package com.example.pet_universe.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ListingRepository(private val listingDatabaseDao: ListingDatabaseDao) {
    val allListings: Flow<List<Listing>> = listingDatabaseDao.getAll()

    fun insert(listing: Listing) {
        CoroutineScope(IO).launch {
            listingDatabaseDao.insert(listing)
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

}
