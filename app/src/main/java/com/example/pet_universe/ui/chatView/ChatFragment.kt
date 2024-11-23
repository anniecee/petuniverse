package com.example.pet_universe.ui.chatView

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pet_universe.R
import com.example.pet_universe.database.ChatRepository
import com.example.pet_universe.database.ListingDatabase
import com.example.pet_universe.database.ListingRepository
import com.example.pet_universe.database.Message
import com.example.pet_universe.database.MessageRepository
import com.example.pet_universe.databinding.FragmentChatBinding
import com.example.pet_universe.ui.chat.ChatViewModel
import com.example.pet_universe.ui.chat.ChatViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import java.util.Date

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var messageAdapter: MessageAdapter

    private val args: ChatFragmentArgs by navArgs()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private lateinit var chatId: String
    private lateinit var receiverId: String
    private var listingId: Long = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val database = ListingDatabase.getInstance(requireContext())
        val chatRepository = ChatRepository(database.chatDao)
        val messageRepository = MessageRepository(database.messageDao)
        val listingRepository = ListingRepository(database.listingDao)
        val factory = ChatViewModelFactory(chatRepository, messageRepository, listingRepository)
        chatViewModel = ViewModelProvider(this, factory).get(ChatViewModel::class.java)

        chatId = args.chatId
        receiverId = args.receiverId
        listingId = args.listingId

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        messageAdapter = MessageAdapter()
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.messagesRecyclerView.adapter = messageAdapter

        // Observe messages
        chatViewModel.messagesLiveData.observe(viewLifecycleOwner) { messages ->
            messageAdapter.submitList(messages)
            // Scroll to the latest message
            binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
        }

        // Load messages for the chat
        chatViewModel.getMessagesForChat(chatId)

        // Handle send button click
        binding.sendButton.setOnClickListener {
            val content = binding.messageEditText.text.toString().trim()
            if (content.isNotEmpty()) {
                val message = Message(
                    chatId = chatId,
                    listingId = listingId,
                    senderId = currentUserId,
                    receiverId = receiverId,
                    content = content,
                    timestamp = Date()
                )
                chatViewModel.sendMessage(message)
                binding.messageEditText.text.clear()
            } else {
                Toast.makeText(requireContext(), "Cannot send empty message", Toast.LENGTH_SHORT).show()
            }
        }

        // Optionally, disable send button when there's no text
        binding.messageEditText.addTextChangedListener { text ->
            binding.sendButton.isEnabled = !text.isNullOrBlank()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}