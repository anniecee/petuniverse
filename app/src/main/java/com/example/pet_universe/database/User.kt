package com.example.pet_universe.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity(tableName = "user_table")
@TypeConverters(Converters::class)
data class User (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "first_name")
    var firstName: String = "",

    @ColumnInfo(name = "last_name")
    var lastName: String = "",

    @ColumnInfo(name = "email")
    var email: String = "",

    @ColumnInfo(name = "password")
    var password: String = "",

    @ColumnInfo(name = "profile_photo")
    var profilePhoto: ByteArray = byteArrayOf(),

    @ColumnInfo(name = "buy_listing_id_list")
    var buyListingIdList: List<Long> = listOf(),

    @ColumnInfo(name = "sell_listing_id_list")
    var sellListingIdList: List<Long> = listOf(),

    @ColumnInfo(name = "fav_listing_id_list")
    var favListingIdList: List<Long> = listOf()
)
