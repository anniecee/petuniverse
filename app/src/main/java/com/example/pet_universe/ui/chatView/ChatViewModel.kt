package com.example.pet_universe.ui.chat

import androidx.lifecycle.*
import com.example.pet_universe.database.Chat
import com.example.pet_universe.database.ChatRepository
import com.example.pet_universe.database.Listing
import com.example.pet_universe.database.ListingRepository
import com.example.pet_universe.database.Message
import com.example.pet_universe.database.MessageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val listingRepository: ListingRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""

    private val _chatsLiveData = MutableLiveData<List<Chat>>()
    val chatsLiveData: LiveData<List<Chat>> get() = _chatsLiveData

    private val _messagesLiveData = MutableLiveData<List<Message>>()
    val messagesLiveData: LiveData<List<Message>> get() = _messagesLiveData

    init {
        syncChatsFromFirebase()
    }

    fun getChatsForUser() {
        viewModelScope.launch {
            chatRepository.getChatsForUser(currentUserId).collect { chats ->
                val chatsWithUsernames = chats.map { chat ->
                    val otherUserId = if (chat.userId1 == currentUserId) chat.userId2 else chat.userId1
                    val otherUserName = fetchUserName(otherUserId)
                    val listing = getListingById(chat.listingId)
                    chat.copy(otherUserName = otherUserName,
                        listingTitle = listing?.title ?: "Unknown Listing",
                        listingImageUrl = listing?.imageUrl ?: ""
                    )
                }
                _chatsLiveData.postValue(chatsWithUsernames)
            }
        }
    }

    private suspend fun fetchUserName(userId: String): String {
        return try {
            val documentSnapshot = firestore.collection("users").document(userId).get().await()
            documentSnapshot.getString("first_name") ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun getMessagesForChat(chatId: String) {
        viewModelScope.launch {
            messageRepository.getMessagesForChat(chatId).collect { messages ->
                _messagesLiveData.postValue(messages)
            }
        }
        syncMessagesFromFirebase(chatId)
    }

    fun sendMessage(message: Message) {
        viewModelScope.launch {
            // Generate a new message ID
            val messageRef = firestore.collection("chats").document(message.chatId)
                .collection("messages").document() // Generate new document reference
            val messageId = messageRef.id
            val messageWithId = message.copy(id = messageId)

            // Insert into local database
           // messageRepository.insert(messageWithId)

            // Sync message to Firebase
            messageRef.set(messageWithId.toMap())
                .addOnSuccessListener {
                    // Insert into local database after successful upload
                    viewModelScope.launch {
                        messageRepository.insert(messageWithId)
                    }
                }

                .addOnFailureListener { e ->
                    println("Error sending message to Firebase: $e")
                }

            // Update chat
            val chat = chatRepository.getChatById(message.chatId) ?: Chat(
                chatId = message.chatId,
                userId1 = message.senderId,
                userId2 = message.receiverId,
                listingId = message.listingId
            )
            chat.lastMessage = message.content
            chat.lastTimestamp = message.timestamp
            chatRepository.insert(chat)
            // Sync to Firebase
           // syncMessageToFirebase(message)
            syncChatToFirebase(chat)
        }
    }

    private fun syncChatsFromFirebase() {
        firestore.collection("chats")
            .whereArrayContains("userIds", currentUserId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Error syncing chats: $e")
                    return@addSnapshotListener
                }
                if(snapshot != null) {
                    for (doc in snapshot.documents) {
                        val chat = doc.toObject(Chat::class.java)
                        if (chat != null) {
                            viewModelScope.launch {
                                chatRepository.insert(chat)
                            }
                        }
                    }
                }
            }
    }

    private fun syncMessagesFromFirebase(chatId: String) {
        firestore.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Error syncing messages: $e")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    for (docChange in snapshot.documentChanges) {
                        val doc = docChange.document
                        val message = doc.toObject(Message::class.java).copy(id = doc.id)
                        if (message != null) {
                            viewModelScope.launch {
                                // Check if message already exists
                                val existingMessage = messageRepository.getMessageById(message.id)
                                if (existingMessage == null) {
                                    messageRepository.insert(message)
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun syncMessageToFirebase(message: Message) {
        firestore.collection("chats").document(message.chatId)
            .collection("messages")
            .add(message.toMap())
            .addOnFailureListener { e ->
                println("Error sending message to Firebase: $e")
            }
    }

    private fun syncChatToFirebase(chat: Chat) {
        firestore.collection("chats").document(chat.chatId)
            .set(chat.toMap())
            .addOnFailureListener { e ->
                println("Error updating chat in Firebase: $e")
            }
    }

    private suspend fun getListingById(listingId: Long): Listing? {
        return withContext(Dispatchers.IO) {
            listingRepository.getListingById(listingId)
        }
    }
}

class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val listingRepository: ListingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatRepository, messageRepository, listingRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
