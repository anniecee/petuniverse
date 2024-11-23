// ChatListAdapter.kt
package com.example.pet_universe.ui.chatView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.pet_universe.R
import com.example.pet_universe.database.Chat
import com.example.pet_universe.database.ListingViewModel
import com.example.pet_universe.databinding.ItemChatBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class ChatListAdapter(private val onChatClick: (Chat) -> Unit) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    private var chatList = listOf<Chat>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun getItemCount(): Int = chatList.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    fun submitList(chats: List<Chat>) {
        chatList = chats
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.lastMessageTextView.text = chat.lastMessage
            val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            binding.timestampTextView.text = dateFormat.format(chat.lastTimestamp)
            binding.usernameTextView.text = chat.otherUserName
            // Fetch listing information
            binding.listingTitleTextView.text = chat.listingTitle
            binding.listingImageView.load(chat.listingImageUrl) {
                placeholder(R.drawable.live_pets_logo)
                error(R.drawable.live_pets_logo)
            }
            binding.root.setOnClickListener { onChatClick(chat) }
        }
    }
}
