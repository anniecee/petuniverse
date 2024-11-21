package com.example.pet_universe.ui.buyView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.pet_universe.R
import com.example.pet_universe.databinding.FragmentIndividualListingBinding
import com.example.pet_universe.ui.profile.ProfileViewModel

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

        listingsViewModel.selectedListing.observe(viewLifecycleOwner) { listing ->
            binding.listingNameTextView.text = listing.title
            binding.listingPriceTextView.text = "$${listing.price}" // Format price with dollar sign
            binding.listingTypeTextView.text = listing.type
            binding.listingDescriptionTextView.text = listing.description
            binding.locationTextView.text = listing.meetingLocation

            // Load the first image from the imageUrls list using Coil
            if (listing.imageUrls.isNotEmpty()) {
                val imageUrl = listing.imageUrls[0]
                binding.listingImageView.load(imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.pet_accessories_logo)
                    error(R.drawable.pet_accessories_logo)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}