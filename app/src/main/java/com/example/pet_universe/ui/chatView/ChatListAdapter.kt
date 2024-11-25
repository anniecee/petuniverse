// ChatListAdapter.kt
package com.example.pet_universe.ui.chatView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.pet_universe.R
import com.example.pet_universe.database.Chat
import com.example.pet_universe.databinding.ItemChatBinding
import java.text.SimpleDateFormat
import java.util.*

class ChatListAdapter(private val onChatClick: (Chat) -> Unit) :
    RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

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

    fun formatTimestamp(timestamp: Date): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp.time

        return when {
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m" // Minutes ago
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h" // Hours ago
            diff < 7 * 24 * 60 * 60 * 1000 -> SimpleDateFormat("EEE.", Locale.getDefault()).format(
                timestamp
            ) // Day of the week
            else -> SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(timestamp) // Full date
        }
    }

    inner class ChatViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.lastMessageTextView.text = chat.lastMessage
            binding.timestampTextView.text = formatTimestamp(chat.lastTimestamp)
            binding.usernameTextView.text = chat.otherUserName
            binding.listingTitleTextView.text = chat.listingTitle
            binding.listingImageView.load(chat.listingImageUrl) {
                placeholder(R.drawable.image_placeholder)
                error(R.drawable.image_placeholder)
            }
            binding.root.setOnClickListener { onChatClick(chat) }
        }
    }
}
