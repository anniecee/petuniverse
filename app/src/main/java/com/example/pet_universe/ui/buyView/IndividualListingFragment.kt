package com.example.pet_universe.ui.buyView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.pet_universe.R
import com.example.pet_universe.database.ListingDatabase
import com.example.pet_universe.database.RatingRepository
import com.example.pet_universe.databinding.FragmentIndividualListingBinding
import com.example.pet_universe.ui.rating.RatingViewModel
import com.example.pet_universe.ui.rating.RatingViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class IndividualListingFragment : Fragment() {

    private var _binding: FragmentIndividualListingBinding? = null
    private val binding get() = _binding!!
    private val listingsViewModel: ListingsViewModel by activityViewModels()
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var ratingViewModel: RatingViewModel
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIndividualListingBinding.inflate(inflater, container, false)

        val database = ListingDatabase.getInstance(requireContext())
        val ratingRepository = RatingRepository(database.ratingDao)
        val factory = RatingViewModelFactory(ratingRepository)
        ratingViewModel = ViewModelProvider(this, factory).get(RatingViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listing = listingsViewModel.selectedListing.value
        if (listing != null) {
         //   val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val sellerId = listing.sellerId ?: ""

            // for starting the chat
            if (sellerId == currentUserId) {
                //rating
                binding.sellerRatingBar.visibility = View.GONE
                binding.rateSellerTextView.visibility = View.GONE

                binding.listingSoldByTextView.visibility = View.GONE
                binding.listingSellerImageView.visibility = View.GONE
                binding.listingSellerTextView.visibility = View.GONE
                binding.startChatButton.visibility = View.GONE
                binding.editListingButton.setOnClickListener {
                    findNavController().navigate(R.id.navigation_seller)
                }
            } else {
                lifecycleScope.launch {
                    val sellerName = fetchSellerName(sellerId)
                    binding.listingSellerTextView.text = "$sellerName"

                    //for making the rating visible in the individual listing fragment
                    val averageRating = ratingViewModel.getAverageRating(sellerId)
                    binding.sellerRatingBar.rating = averageRating
                }

                // Navigate to RatingFragment when clicking "Rate Seller"
                binding.rateSellerTextView.setOnClickListener {
                    val action =
                        IndividualListingFragmentDirections.actionIndividualListingFragmentToRatingFragment(
                            toUserId = sellerId,
                            listingId = listing.id
                        )
                    findNavController().navigate(action)
                }

                binding.editListingButton.visibility = View.GONE
                binding.startChatButton.setOnClickListener {
                    val chatId = generateChatId(currentUserId, sellerId, listing.id)
                    val action =
                        IndividualListingFragmentDirections.actionIndividualListingFragmentToChatFragment(
                            chatId = chatId,
                            receiverId = sellerId,
                            listingId = listing.id
                        )
                    findNavController().navigate(action)
                }
            }
        }

        listingsViewModel.selectedListing.observe(viewLifecycleOwner) { listing ->
            binding.listingNameTextView.text = listing.title
            binding.listingPriceTextView.text =
                "Price: $${listing.price}" // Format price with dollar sign
            binding.listingTypeTextView.text = "Type: ${listing.type}"
            binding.listingDescriptionTextView.text = listing.description
            binding.locationTextView.text = "Location: ${listing.meetingLocation}"

            // Load the first image from the imageUrls list using Coil
            if (listing.imageUrl != null) {
                val imageUrl = listing.imageUrl
                binding.listingImageView.load(imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.image_placeholder)
                    error(R.drawable.image_placeholder)
                }
            }
        }
    }

    private suspend fun fetchSellerName(sellerId: String): String {
        return try {
            val documentSnapshot = firestore.collection("users").document(sellerId).get().await()
            documentSnapshot.getString("firstName") ?: "Seller"
        } catch (e: Exception) {
            "Seller"
        }
    }

    // Function to generate chatId
    private fun generateChatId(userId1: String, userId2: String, listingId: Long): String {
        return if (userId1 < userId2) "${userId1}_${userId2}_${listingId}" else "${userId2}_${userId1}_${listingId}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}