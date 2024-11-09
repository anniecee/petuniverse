package com.example.pet_universe.ui.livePets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pet_universe.R
import com.example.pet_universe.database.ListingDatabase
import com.example.pet_universe.database.ListingDatabaseDao
import com.example.pet_universe.database.ListingRepository
import com.example.pet_universe.database.ListingViewModel
import com.example.pet_universe.database.ListingViewModelFactory
import com.example.pet_universe.databinding.FragmentLivePetsBinding
import com.example.pet_universe.ui.dialogs.MyDialog
import com.example.pet_universe.ui.profile.ProfileViewModel
import com.google.firebase.firestore.FirebaseFirestore

class LivePetsFragment : Fragment() {

    //Firestore database
    private lateinit var firestore: FirebaseFirestore

    // Initialization for database
    private lateinit var database: ListingDatabase
    private lateinit var listingDao: ListingDatabaseDao
    private lateinit var repository: ListingRepository
    private lateinit var viewModelFactory: ListingViewModelFactory
    private lateinit var listingViewModel: ListingViewModel

    private var _binding: FragmentLivePetsBinding? = null
    private val binding get() = _binding!!
    private val livePetsViewModel: LivePetsViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    private val petTypes = listOf("All", "Dogs", "Cats", "Birds", "Snakes", "Others")
    private val ageRange = listOf("All", "Puppy/Kitten", "Young", "Adult", "Senior")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLivePetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Set up profile icon
        profileViewModel.userInitial.observe(viewLifecycleOwner) { initial ->
            binding.root.findViewById<TextView>(R.id.profileIcon).text = initial ?: ""
        }

        // Navigate to AccountSettingsFragment on profileIcon click
        binding.root.findViewById<TextView>(R.id.profileIcon).setOnClickListener {
            findNavController().navigate(R.id.action_global_to_accountSettings)
        }

        // Set up database and ViewModel
        database = ListingDatabase.getInstance(requireContext())
        listingDao = database.listingDao
        repository = ListingRepository(listingDao)
        viewModelFactory = ListingViewModelFactory(repository)
        listingViewModel = ViewModelProvider(this, viewModelFactory).get(ListingViewModel::class.java)

        // Initialize RecyclerView with an empty list initially
        binding.petRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val petAdapter = PetAdapter(listOf()) { pet ->
            livePetsViewModel.selectPet(pet)
            findNavController().navigate(R.id.action_livePetsFragment_to_individualPetFragment)
        }
        binding.petRecyclerView.adapter = petAdapter

        // Observe listings from Firebase and update adapter
        listingViewModel.fetchListingsFromFirebase()

        // Observe listings from ViewModel and update adapter
        listingViewModel.allListingsLiveData.observe(viewLifecycleOwner) { listings ->
            val pets = listings.map { listing ->
                Pet(
                    name = listing.title,
                    price = listing.price,
                    description = listing.description,
                    imageResId = 0, // PLACEHOLDER VALUE, for future image support
                    petLocation = listing.meetingLocation
                )
            }
            petAdapter.updatePets(pets)
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
        val dialog = MyDialog(requireContext(), "Select Age Range", ageRange) { selectedAgeRange ->
            livePetsViewModel.setSelectedAgeRange(selectedAgeRange)
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
