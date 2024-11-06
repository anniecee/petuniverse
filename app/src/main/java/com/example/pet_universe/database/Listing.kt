package com.example.pet_universe.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Calendar

@Entity(tableName = "listing_table")
@TypeConverters(Converters::class)
data class Listing(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "title")
    var title: String = "",

    @ColumnInfo(name = "description")
    var description: String = "",

    @ColumnInfo(name = "price")
    var price: Double = 0.0,

    @ColumnInfo(name = "category")
    var category: String = "",

    @ColumnInfo(name = "photo", typeAffinity = ColumnInfo.BLOB)
    var photo: ByteArray = byteArrayOf(),

    // Save user id of seller when listing is created
    @ColumnInfo(name = "seller_id")
    var sellerId: Long = 0L,

    // Save user id of buyer when listing is sold
    @ColumnInfo(name = "buyer_id")
    var buyerId: Long = 0L,

    // TODO: Need to check & use appropriate datatype
    @ColumnInfo(name = "meeting_location")
    var meetingLocation: String = "",

    // Save date and time of meeting when listing is sold
    @ColumnInfo(name = "meeting_date_time")
    var meetingDateTime: Calendar = Calendar.getInstance(),

    @ColumnInfo(name = "is_sold")
    var isSold: Boolean = false
)
