// MessageAdapter.kt
package com.example.pet_universe.ui.chatView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pet_universe.database.Message
import com.example.pet_universe.databinding.ItemMessageReceivedBinding
import com.example.pet_universe.databinding.ItemMessageSentBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        return if (viewType == VIEW_TYPE_SENT) {
            val binding =
                ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SentMessageViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ReceivedMessageViewHolder(binding)
        }
    }

    override fun getItemCount(): Int = messagesList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messagesList[position]
        val formattedTime = formatTimestamp(message.timestamp)

        when (holder) {
            is SentMessageViewHolder -> {
                holder.binding.apply {
                    sentMessageTextView.text = message.content
                    sentTimestampTextView.text = formattedTime
                }
            }

            is ReceivedMessageViewHolder -> {
                holder.binding.apply {
                    receivedMessageTextView.text = message.content
                    receivedTimestampTextView.text = formattedTime
                }
            }
        }
    }

    private fun formatTimestamp(timestamp: Date): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp.time

        return when {
            diff < 24 * 60 * 60 * 1000 && SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(
                timestamp
            ) ==
                    SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(now)) ->
                "Today, " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp) // Today
            diff < 2 * 24 * 60 * 60 * 1000 ->
                "Yesterday, " + SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(timestamp) // Yesterday
            else -> SimpleDateFormat(
                "MMM dd, HH:mm",
                Locale.getDefault()
            ).format(timestamp) // Date and time
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