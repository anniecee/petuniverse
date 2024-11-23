// MessageAdapter.kt
package com.example.pet_universe.ui.chatView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pet_universe.database.Message
import com.example.pet_universe.databinding.ItemMessageReceivedBinding
import com.example.pet_universe.databinding.ItemMessageSentBinding
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messagesList = listOf<Message>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messagesList[position].senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SentMessageViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ReceivedMessageViewHolder(binding)
        }
    }

    override fun getItemCount(): Int = messagesList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messagesList[position]
        if (holder is SentMessageViewHolder) {
            holder.binding.sentMessageTextView.text = message.content
        } else if (holder is ReceivedMessageViewHolder) {
            holder.binding.receivedMessageTextView.text = message.content
        }
    }

    fun submitList(messages: List<Message>) {
        messagesList = messages
        notifyDataSetChanged()
    }

    inner class SentMessageViewHolder(val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ReceivedMessageViewHolder(val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root)
}
