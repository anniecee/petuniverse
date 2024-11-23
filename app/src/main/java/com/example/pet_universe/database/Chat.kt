package com.example.pet_universe.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Calendar
import java.util.Date

@Entity(tableName = "chat_table")
@TypeConverters(Converters::class)
data class Chat(
    @PrimaryKey
    var chatId: String = "",
    var userId1: String = "",
    var userId2: String = "",
    var listingId: Long = 0L,
    var lastMessage: String = "",
    var lastTimestamp: Date = Date(),
    var otherUserName: String = "",
    var listingTitle: String = "",          // New field
    var listingImageUrl: String = ""
) {
    // Convert to Map for Firebase
    fun toMap(): Map<String, Any> = mapOf(
        "chatId" to chatId,
        "userId1" to userId1,
        "userId2" to userId2,
        "listingId" to listingId,
        "lastMessage" to lastMessage,
        "lastTimestamp"  to com.google.firebase.Timestamp(lastTimestamp)
    )
}
