package com.example.pet_universe.ui.accountSettings


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pet_universe.database.Rating
import com.example.pet_universe.databinding.ItemRatingBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class RatingsAdapter : RecyclerView.Adapter<RatingsAdapter.RatingViewHolder>() {

    private var ratingsList = listOf<Rating>()
    private val firestore = FirebaseFirestore.getInstance()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val userNamesCache = mutableMapOf<String, String>()

    fun submitList(ratings: List<Rating>) {
        ratingsList = ratings
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingViewHolder {
        val binding = ItemRatingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RatingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RatingViewHolder, position: Int) {
        val rating = ratingsList[position]
        holder.bind(rating)
    }

    override fun getItemCount(): Int {
        return ratingsList.size
    }

    inner class RatingViewHolder(private val binding: ItemRatingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(rating: Rating) {
            // Bind the rating data to the UI elements
            binding.ratingBar.rating = rating.ratingValue.toFloat()
            binding.ratingValueTextView.text = "Rating: ${rating.ratingValue}"

            // Display the date
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.timestampTextView.text = "Date: ${formatter.format(rating.timestamp)}"

            // Fetch and display the name of the user who gave the rating
            val fromUserId = rating.fromUserId
            binding.fromUserTextView.text = "From: Loading..."

            // fetching the review text
            if (rating.reviewText.isNotEmpty()) {
                binding.reviewTextView.text = rating.reviewText
                binding.reviewTextView.visibility = View.VISIBLE
            } else {
                binding.reviewTextView.visibility = View.GONE
            }

            if (userNamesCache.containsKey(fromUserId)) {
                binding.fromUserTextView.text = "From: ${userNamesCache[fromUserId]}"
            } else {
                coroutineScope.launch {
                    val userName = fetchUserName(fromUserId)
                    userNamesCache[fromUserId] = userName
                    binding.fromUserTextView.text = "From: $userName"
                }
            }
        }

        private suspend fun fetchUserName(userId: String): String {
            return withContext(Dispatchers.IO) {
                try {
                    val documentSnapshot = firestore.collection("users").document(userId).get().await()
                    val firstName = documentSnapshot.getString("firstName") ?: ""
                    val lastName = documentSnapshot.getString("lastName") ?: ""
                    "$firstName $lastName"
                } catch (e: Exception) {
                    "Unknown User"
                }
            }
        }
    }
}
