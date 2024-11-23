package com.example.pet_universe.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessageRepository(private val messageDao: MessageDao) {
    suspend fun insert(message: Message) {
        messageDao.insert(message)
    }

    fun getMessagesForChat(chatId: String): Flow<List<Message>> {
        return messageDao.getMessagesForChat(chatId)
    }

    suspend fun getMessageById(messageId: String): Message? {
        return messageDao.getMessageById(messageId)
    }
}
