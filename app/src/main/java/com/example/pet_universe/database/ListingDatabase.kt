package com.example.pet_universe.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Listing::class], version = 1)
abstract class ListingDatabase: RoomDatabase() {
    abstract val listingDao: ListingDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: ListingDatabase? = null

        fun getInstance(context: Context): ListingDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(context, ListingDatabase::class.java, "listing_database").build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
