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
import com.example.pet_universe.database.MessageRepository
import com.example.pet_universe.database.RatingRepository
import com.example.pet_universe.database.Listing
import com.example.pet_universe.database.ListingDatabaseDao
import com.example.pet_universe.database.ListingRepository
import com.example.pet_universe.database.ListingViewModel
import com.example.pet_universe.database.ListingViewModelFactory
import com.example.pet_universe.databinding.FragmentIndividualListingBinding
import com.example.pet_universe.ui.rating.RatingViewModel
import com.example.pet_universe.ui.rating.RatingViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class IndividualListingFragment : Fragment() {

    private var _binding: FragmentIndividualListingBinding? = null
    private val binding get() = _binding!!
    private val listingsViewModel: ListingsViewModel by activityViewModels()
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var ratingViewModel: RatingViewModel
    private lateinit var individualListingViewModel: IndividualListingViewModel
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Room database
    private lateinit var database: ListingDatabase
    private lateinit var listingDao: ListingDatabaseDao
    private lateinit var repository: ListingRepository
    private lateinit var viewModelFactory: ListingViewModelFactory
    private lateinit var listingViewModel: ListingViewModel

    private lateinit var favListRef: CollectionReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIndividualListingBinding.inflate(inflater, container, false)

        val database = ListingDatabase.getInstance(requireContext())
        val ratingRepository = RatingRepository(database.ratingDao)
        val factory = RatingViewModelFactory(ratingRepository)
        ratingViewModel = ViewModelProvider(this, factory).get(RatingViewModel::class.java)

        //This will be used to check if to show the give rating feature to the user or not
        // meaning check to see if their is an interaction that happened between user or seller
        val messageRepository = MessageRepository(database.messageDao)
        val individualListingFactory = IndividualListingViewModelFactory(messageRepository)
        individualListingViewModel = ViewModelProvider(this, individualListingFactory).get(IndividualListingViewModel::class.java)

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
                    val sellerName = fetchSellerName(sellerId) ?: "Seller"
                    binding.listingSellerTextView.text = "$sellerName"

                    //for making the rating visible in the individual listing fragment
                    val averageRating = ratingViewModel.getAverageRating(sellerId)
                    binding.sellerRatingBar.rating = averageRating
                }

                val chatId = generateChatId(currentUserId, sellerId, listing.id)

                // Fetch message counts to determine if "Rate Seller" should be shown
                individualListingViewModel.fetchMessageCounts(chatId, sellerId)
                // Observe the canRateSeller LiveData
                individualListingViewModel.canRateSeller.observe(viewLifecycleOwner) { canRate ->
                    if (canRate) {
                        binding.rateSellerTextView.visibility = View.VISIBLE
                    } else {
                        binding.rateSellerTextView.visibility = View.GONE
                    }
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
                  //  val chatId = generateChatId(currentUserId, sellerId, listing.id)
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

        // Set up favorite button if listing is in favorites list
        favListRef = firestore.collection("users").document(currentUserId).collection("favorites")
        val docRef = favListRef.document(listing?.id.toString())
        docRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                binding.favButton.setBackgroundResource(R.drawable.ic_favorite_red)
            } else {
                binding.favButton.setBackgroundResource(R.drawable.ic_favorite_border)
            }
        }

        // Update favorite list when button is toggled
        binding.favButton.setOnClickListener {
            if (listing != null) {
                docRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        docRef.delete()
                        binding.favButton.setBackgroundResource(R.drawable.ic_favorite_border)
                        listing.isFav = false
                        listingViewModel.updateListing(listing)
                    } else {
                        addToFavList(listing)
                        binding.favButton.setBackgroundResource(R.drawable.ic_favorite_red)
                        listing.isFav = true
                        listingViewModel.updateListing(listing)
                    }
                }
            }
        }
    }

    private fun addToFavList(listing: Listing) {
        // Add to favorite list in Firestore
        val docRef = favListRef.document(listing.id.toString())

        val favListing = hashMapOf(
            "id" to listing.id,
            "title" to listing.title,
            "description" to listing.description,
            "category" to listing.category,
            "type" to listing.type,
            "price" to listing.price,
            "meetingLocation" to listing.meetingLocation,
            "sellerId" to listing.sellerId,
            "imageUrl" to listing.imageUrl,
        )

        docRef.set(favListing)
            .addOnSuccessListener {
                println("Listing updated in Firebase")
            }
            .addOnFailureListener { e ->
                println("Error updating listing in Firebase: $e")
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