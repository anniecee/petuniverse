package com.example.pet_universe.ui.buyView

import androidx.lifecycle.*
import com.example.pet_universe.database.MessageRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class IndividualListingViewModel(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _canRateSeller = MutableLiveData<Boolean>()
    val canRateSeller: LiveData<Boolean> get() = _canRateSeller

    fun fetchMessageCounts(chatId: String, sellerId: String) {
        viewModelScope.launch {
            messageRepository.getMessagesForChat(chatId).collect { messages ->
                val buyerToSellerMessages = messages.filter {
                    it.senderId == currentUserId && it.receiverId == sellerId
                }
                val sellerToBuyerMessages = messages.filter {
                    it.senderId == sellerId && it.receiverId == currentUserId
                }

                val buyerMessageCount = buyerToSellerMessages.size
                val sellerMessageCount = sellerToBuyerMessages.size

                // Update the LiveData based on the specified thresholds
                val canRate = buyerMessageCount >= 4 && sellerMessageCount >= 3
                _canRateSeller.postValue(canRate)
            }
        }
    }
}

class IndividualListingViewModelFactory(
    private val messageRepository: MessageRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IndividualListingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IndividualListingViewModel(messageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
