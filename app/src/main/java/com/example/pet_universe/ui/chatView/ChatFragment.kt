package com.example.pet_universe.ui.chatView

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pet_universe.database.ChatRepository
import com.example.pet_universe.database.ListingDatabase
import com.example.pet_universe.database.ListingRepository
import com.example.pet_universe.database.Message
import com.example.pet_universe.database.MessageRepository
import com.example.pet_universe.databinding.FragmentChatBinding
import com.example.pet_universe.ui.chatView.ChatViewModel
import com.example.pet_universe.ui.chatView.ChatViewModelFactory
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView
        messageAdapter = MessageAdapter()
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.messagesRecyclerView.adapter = messageAdapter

        // Observe messages
        chatViewModel.messagesLiveData.observe(viewLifecycleOwner) { messages ->
            messageAdapter.submitList(messages)
            binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
        }

        // Observe chats to get user name and listing title
        chatViewModel.chatsLiveData.observe(viewLifecycleOwner) { chats ->
            chats.find { it.chatId == chatId }?.let { chat ->
                binding.userNameTextView.text = chat.otherUserName
                binding.listingTitleTextView.text = chat.listingTitle
            }
        }

        // Load data
        chatViewModel.getChatsForUser()
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
                Toast.makeText(requireContext(), "Cannot send empty message.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Optionally, disable send button when there's no text
        binding.messageEditText.addTextChangedListener { text ->
            binding.sendButton.isEnabled = !text.isNullOrBlank()
        }

        setupKeyboardListener()
    }

    private fun setupKeyboardListener() {
        val rootView = binding.chatFragment
        val messageInputLayout = binding.messageInputLayout
        val recyclerView = binding.messagesRecyclerView

        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)

            val screenHeight = rootView.rootView.height
            val keyboardHeight = screenHeight - rect.bottom

            if (keyboardHeight > screenHeight * 0.15) {
                // Move the input bar precisely above the keyboard
                val inputBarHeight = messageInputLayout.height
                messageInputLayout.translationY = -(keyboardHeight - inputBarHeight).toFloat()

                // Adjust RecyclerView padding for extra space
                recyclerView.setPadding(0, 0, 0, keyboardHeight - inputBarHeight)
            } else {
                // Reset when the keyboard is dismissed
                messageInputLayout.translationY = 0f
                recyclerView.setPadding(0, 0, 0, 0)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}