package com.example.pet_universe.ui.livePets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pet_universe.R
import com.example.pet_universe.databinding.FragmentLivePetsBinding
import com.example.pet_universe.ui.dialogs.MyDialog
import com.example.pet_universe.ui.profile.ProfileViewModel

class LivePetsFragment : Fragment() {

    private var _binding: FragmentLivePetsBinding? = null
    private val binding get() = _binding!!
    private val livePetsViewModel: LivePetsViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    private val petTypes = listOf("Dogs", "Cats", "Birds", "Snakes", "Others")
    private val ageRanges = listOf("Puppy/Kitten", "Young", "Adult", "Senior")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLivePetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel.userInitial.observe(viewLifecycleOwner) { initial ->
            binding.root.findViewById<TextView>(R.id.profileIcon).text = initial
        }

        // Navigate to AccountSettingsFragment on profileIcon click
        binding.root.findViewById<TextView>(R.id.profileIcon).setOnClickListener {
            findNavController().navigate(R.id.action_global_to_accountSettings)
        }

        val petList = listOf(
            Pet("Buddy", "Male", "Medium • Labrador • 2 miles away", R.drawable.pet_food_logo),
            Pet("Lucy", "Female", "Small • Beagle • 5 miles away", R.drawable.live_pets_logo),
            Pet("Max", "Male", "Large • German Shepherd • 3 miles away", R.drawable.pet_food_logo)
        )

        binding.petRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.petRecyclerView.adapter = PetAdapter(petList) { pet ->
            livePetsViewModel.selectPet(pet)
            findNavController().navigate(R.id.action_livePetsFragment_to_individualPetFragment)
        }

        binding.petFilterButton.setOnClickListener {
            showPetFilterDialog()
        }

        binding.ageFilterButton.setOnClickListener {
            showAgeFilterDialog()
        }


        livePetsViewModel.selectedPetType.observe(viewLifecycleOwner) { petType ->
            binding.petFilterButton.text = petType
        }

        livePetsViewModel.selectedAgeRange.observe(viewLifecycleOwner) { ageRange ->
            binding.ageFilterButton.text = ageRange
        }
    }

    private fun showPetFilterDialog() {
        val dialog = MyDialog(requireContext(), "Select Pet Type", petTypes) { selectedPetType ->
            livePetsViewModel.setSelectedPetType(selectedPetType)
        }
        dialog.show()
    }

    private fun showAgeFilterDialog() {
        val dialog = MyDialog(requireContext(), "Select Age Range", ageRanges) { selectedAgeRange ->
            livePetsViewModel.setSelectedAgeRange(selectedAgeRange)
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
