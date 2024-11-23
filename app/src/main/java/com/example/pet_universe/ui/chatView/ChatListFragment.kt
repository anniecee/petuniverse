package com.example.pet_universe.ui.chatView

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pet_universe.R
import com.example.pet_universe.database.ChatRepository
import com.example.pet_universe.database.ListingDatabase
import com.example.pet_universe.database.ListingRepository
import com.example.pet_universe.database.MessageRepository
import com.example.pet_universe.databinding.FragmentChatListBinding
import com.example.pet_universe.ui.chat.ChatViewModel
import com.example.pet_universe.ui.chat.ChatViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatListAdapter: ChatListAdapter

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        val database = ListingDatabase.getInstance(requireContext())
        val chatRepository = ChatRepository(database.chatDao)
        val messageRepository = MessageRepository(database.messageDao)
        val listingRepository = ListingRepository(database.listingDao)
        val factory = ChatViewModelFactory(chatRepository, messageRepository, listingRepository)
        chatViewModel = ViewModelProvider(this, factory).get(ChatViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        chatListAdapter = ChatListAdapter { chat ->
            val otherUserId = if (chat.userId1 == currentUserId) chat.userId2 else chat.userId1
            val action = ChatListFragmentDirections.actionChatListFragmentToChatFragment(
                chatId = chat.chatId,
                receiverId = otherUserId,
                listingId = chat.listingId
            )
            findNavController().navigate(action)
        }
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = chatListAdapter

        chatViewModel.chatsLiveData.observe(viewLifecycleOwner) { chats ->
            chatListAdapter.submitList(chats)
        }
        chatViewModel.getChatsForUser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
