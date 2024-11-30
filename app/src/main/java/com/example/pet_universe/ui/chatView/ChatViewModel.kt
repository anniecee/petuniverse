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

    fun updateFCMToken(token: String) {
        viewModelScope.launch {
            // Store token in Firestore
            firestore.collection("users").document(currentUserId)
                .update("fcmToken", token)
                .addOnFailureListener { e ->
                    println("Error updating FCM token: $e")
                }
        }
    }

    fun getChatsForUser() {
        viewModelScope.launch {
            chatRepository.getChatsForUser(currentUserId).collect { chats ->
                val chatsWithUsernames = chats.map { chat ->
                    val otherUserId =
                        if (chat.userId1 == currentUserId) chat.userId2 else chat.userId1
                    val otherUserName = fetchUserName(otherUserId)
//                    val listing = getListingById(chat.listingId)
                    // Try Firebase first, then fall back to local
                    val listing =
                        fetchListingFromFirebase(chat.listingId) ?: getListingById(chat.listingId)

                    // If we got the listing from Firebase, save it locally
                    if (listing != null) {
                        listingRepository.insert(listing)
                    }

                    chat.copy(
                        otherUserName = otherUserName,
                        listingTitle = listing?.title ?: "Unknown Listing",
                        listingImageUrl = listing?.imageUrl ?: ""
                    )
                }
                _chatsLiveData.postValue(chatsWithUsernames)
            }
        }
    }

    private suspend fun fetchListingFromFirebase(listingId: Long): Listing? {
        return withContext(Dispatchers.IO) {
            try {
                val doc =
                    firestore.collection("listings").document(listingId.toString()).get().await()
                if (doc.exists()) {
                    println("DEBUG: Found listing in Firebase: ${doc.getString("title")}")
                    Listing(
                        id = listingId,
                        title = doc.getString("title") ?: "Unknown Listing",
                        imageUrl = doc.getString("imageUrl") ?: "",
                    )
                } else {
                    println("DEBUG: Listing not found in Firebase: $listingId")
                    null
                }
            } catch (e: Exception) {
                println("DEBUG: Error fetching listing from Firebase: $e")
                null
            }
        }
    }

    private suspend fun fetchUserName(userId: String): String {
        return try {
            val documentSnapshot = firestore.collection("users").document(userId).get().await()
            documentSnapshot.getString("firstName") ?: "Unknown"
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

            // Get receiver's FCM token and sender's name
            val receiverDoc = firestore.collection("users")
                .document(message.receiverId)
                .get()
                .await()

            // Get sender's name (current user)
            val senderDoc = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()

            val senderName = senderDoc.getString("firstName") ?: "Unknown"
            val receiverToken = receiverDoc.getString("fcmToken")

            if (receiverToken != null) {
                // Send notification through Firebase Cloud Functions
                firestore.collection("notifications").add(
                    mapOf(
                        "token" to receiverToken,
                        "title" to "$senderName",
                        "body" to message.content,
                        "type" to "chat",
                        "chatId" to message.chatId,
                        "senderId" to message.senderId
                    )
                )
            }
        }
    }

    private fun syncChatsFromFirebase() {
        // Query where userId1 == currentUserId
        val query1 = firestore.collection("chats")
            .whereEqualTo("userId1", currentUserId)

        // Query where userId2 == currentUserId
        val query2 = firestore.collection("chats")
            .whereEqualTo("userId2", currentUserId)

        // Listen to query1
        query1.addSnapshotListener { snapshot, e ->
            if (e != null) {
                println("Error syncing chats (query1): $e")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                for (docChange in snapshot.documentChanges) {
                    val doc = docChange.document
                    val chat = doc.toObject(Chat::class.java)
                    if (chat != null) {
                        viewModelScope.launch {
                            chatRepository.insert(chat)
                            println("user2 chat: $chat")
                        }
                    }
                }
            }
        }

        // Listen to query2
        query2.addSnapshotListener { snapshot, e ->
            if (e != null) {
                println("Error syncing chats (query2): $e")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                for (docChange in snapshot.documentChanges) {
                    val doc = docChange.document
                    val chat = doc.toObject(Chat::class.java)
                    if (chat != null) {
                        viewModelScope.launch {
                            chatRepository.insert(chat)
                            println("user1 chat: $chat")
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
