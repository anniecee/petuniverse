package com.example.pet_universe.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Calendar
import java.util.Date

@Entity(tableName = "message_table")
@TypeConverters(Converters::class)
data class Message(
    @PrimaryKey
    var id: String = "",
    var chatId: String = "",
    var listingId: Long = 0L,
    var senderId: String = "",
    var receiverId: String = "",
    var content: String = "",
    var timestamp: Date = Date()
) {
    // Convert to Map for Firebase
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "chatId" to chatId,
        "listingId" to listingId,
        "senderId" to senderId,
        "receiverId" to receiverId,
        "content" to content,
        "timestamp" to com.google.firebase.Timestamp(timestamp)
    )
}
