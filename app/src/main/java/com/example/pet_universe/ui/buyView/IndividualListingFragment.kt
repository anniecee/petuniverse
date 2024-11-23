package com.example.pet_universe.ui.buyView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.pet_universe.R
import com.example.pet_universe.databinding.FragmentIndividualListingBinding
import com.example.pet_universe.ui.profile.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

class IndividualListingFragment : Fragment() {

    private var _binding: FragmentIndividualListingBinding? = null
    private val binding get() = _binding!!
    private val listingsViewModel: ListingsViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIndividualListingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up profile icon
        profileViewModel.userInitial.observe(viewLifecycleOwner) { initial ->
            binding.root.findViewById<TextView>(R.id.profileIcon).text = initial ?: ""
        }

        // Navigate to AccountSettingsFragment on profileIcon click
        binding.root.findViewById<TextView>(R.id.profileIcon).setOnClickListener {
            findNavController().navigate(R.id.action_global_to_accountSettings)
        }

        // for starting the chat
        binding.startChatButton.setOnClickListener {
            val listing = listingsViewModel.selectedListing.value
            if (listing != null) {
                val sellerId = listing.sellerId ?: ""
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                if (sellerId == currentUserId) {
                    Toast.makeText(context, "You cannot chat with yourself.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val chatId = generateChatId(currentUserId, sellerId, listing.id)
                val action = IndividualListingFragmentDirections.actionIndividualPetFragmentToChatFragment(
                    chatId = chatId,
                    receiverId = sellerId,
                    listingId = listing.id
                )
                findNavController().navigate(action)
            }
        }

        listingsViewModel.selectedListing.observe(viewLifecycleOwner) { listing ->
            binding.listingNameTextView.text = listing.title
            binding.listingPriceTextView.text = "$${listing.price}" // Format price with dollar sign
            binding.listingTypeTextView.text = listing.type
            binding.listingDescriptionTextView.text = listing.description
            binding.locationTextView.text = listing.meetingLocation

            // Load the first image from the imageUrls list using Coil
            if (listing.imageUrl != null) {
                val imageUrl = listing.imageUrl
                binding.listingImageView.load(imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.pet_accessories_logo)
                    error(R.drawable.pet_accessories_logo)
                }
            }
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