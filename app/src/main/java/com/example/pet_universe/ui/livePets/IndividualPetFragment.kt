package com.example.pet_universe.ui.livePets

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
import com.example.pet_universe.databinding.FragmentIndividualPetBinding
import com.example.pet_universe.ui.profile.ProfileViewModel

class IndividualPetFragment : Fragment() {

    private var _binding: FragmentIndividualPetBinding? = null
    private val binding get() = _binding!!
    private val livePetsViewModel: LivePetsViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIndividualPetBinding.inflate(inflater, container, false)
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

        livePetsViewModel.selectedPet.observe(viewLifecycleOwner) { pet ->
            binding.petNameTextView.text = pet.name
            binding.petPriceTextView.text = "$${pet.price}" // Format price with dollar sign
            binding.petTypeTextView.text = pet.type
            binding.petDescriptionTextView.text = pet.description
            binding.locationTextView.text = pet.petLocation

            // Load the first image from the imageUrls list using Coil
            if (pet.imageUrl != null) {
                val imageUrl = pet.imageUrl
                binding.petImageView.load(imageUrl) {
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