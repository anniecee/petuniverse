package com.example.pet_universe.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRepository(private val chatDao: ChatDao) {
    suspend fun insert(chat: Chat) {
        chatDao.insert(chat)
    }
    suspend fun getChatsForUser(userId: String): Flow<List<Chat>> {
        return chatDao.getChatsForUser(userId)
    }
    suspend fun getChatById(chatId: String): Chat? {
        return chatDao.getChatById(chatId)
    }
}
