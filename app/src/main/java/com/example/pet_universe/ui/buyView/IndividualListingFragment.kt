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
import com.example.pet_universe.database.Listing
import com.example.pet_universe.database.ListingDatabase
import com.example.pet_universe.database.ListingDatabaseDao
import com.example.pet_universe.database.ListingRepository
import com.example.pet_universe.database.ListingViewModel
import com.example.pet_universe.database.ListingViewModelFactory
import com.example.pet_universe.databinding.FragmentIndividualListingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class IndividualListingFragment : Fragment() {

    private var _binding: FragmentIndividualListingBinding? = null
    private val binding get() = _binding!!
    private val listingsViewModel: ListingsViewModel by activityViewModels()
    private val firestore = FirebaseFirestore.getInstance()

    // Room database
    private lateinit var database: ListingDatabase
    private lateinit var listingDao: ListingDatabaseDao
    private lateinit var repository: ListingRepository
    private lateinit var viewModelFactory: ListingViewModelFactory
    private lateinit var listingViewModel: ListingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIndividualListingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up Room database
        database = ListingDatabase.getInstance(requireContext())
        listingDao = database.listingDao
        repository = ListingRepository(listingDao)
        viewModelFactory = ListingViewModelFactory(repository)
        listingViewModel = ViewModelProvider(this, viewModelFactory).get(ListingViewModel::class.java)

        val listing = listingsViewModel.selectedListing.value
        if (listing != null) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val sellerId = listing.sellerId ?: ""

            // for starting the chat
            if (sellerId == currentUserId) {
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

        // Set up favorite button
        binding.favButton.setBackgroundResource(
            if (listing!!.isFav) R.drawable.ic_favorite_red else R.drawable.ic_favorite_border
        )

        // Toggle favorite status when button is clicked
        binding.favButton.setOnClickListener {
            if (listing != null) {
                // Toggle favorite status
                listingsViewModel.toggleFavorite(listing)
                binding.favButton.setBackgroundResource(
                    if (listing.isFav) R.drawable.ic_favorite_red else R.drawable.ic_favorite_border
                )

                // Update favorite status in Firestore & local database
                updateFavStatus(listing)
            }
        }
    }

    private fun updateFavStatus(listing: Listing) {
        // Update listing in Firestore in both global and user-specific collections
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val userListingRef = firestore.collection("users").document(userId).collection("listings")
            .document(listing.id.toString())
        val globalListingRef = firestore.collection("listings").document(listing.id.toString())

        val updatedListing = hashMapOf(
            "isFav" to listing.isFav
        ) as Map<String, Any>

        userListingRef.update(updatedListing)
            .addOnSuccessListener {
                println("Listing updated in Firebase")
            }
            .addOnFailureListener { e ->
                println("Error updating listing in Firebase: $e")
            }

        globalListingRef.update(updatedListing)
            .addOnSuccessListener {
                println("Listing updated in global collection")
            }
            .addOnFailureListener { e ->
                println("Error updating listing in global collection: $e")
            }

        // Update local database
        listingViewModel.updateListing(listing)
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