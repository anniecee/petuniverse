package com.example.pet_universe.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: Chat)

    @Query("SELECT * FROM chat_table WHERE userId1 = :userId OR userId2 = :userId ORDER BY lastTimestamp DESC")
    fun getChatsForUser(userId: String): Flow<List<Chat>>

    @Query("SELECT * FROM chat_table WHERE chatId = :chatId")
    suspend fun getChatById(chatId: String): Chat?

}
