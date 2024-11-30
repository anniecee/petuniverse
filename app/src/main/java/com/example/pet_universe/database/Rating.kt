package com.example.pet_universe.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "rating_table")
@TypeConverters(Converters::class)
data class Rating(
    @PrimaryKey
    var id: String = "", // Use Firebase document ID as the primary key
    var fromUserId: String = "",
    var toUserId: String = "",
    var ratingValue: Int = 0, // Rating from 1 to 5
    var listingId: Long = 0L,
    var timestamp: Date = Date()
) {
    // Convert to Map for Firebase
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "fromUserId" to fromUserId,
        "toUserId" to toUserId,
        "ratingValue" to ratingValue,
        "listingId" to listingId,
        "timestamp" to com.google.firebase.Timestamp(timestamp)
    )
}
