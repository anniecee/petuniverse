package com.example.pet_universe.ui.livePets

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pet_universe.R
import com.example.pet_universe.database.ListingDatabase
import com.example.pet_universe.database.ListingDatabaseDao
import com.example.pet_universe.database.ListingRepository
import com.example.pet_universe.database.ListingViewModel
import com.example.pet_universe.database.ListingViewModelFactory
import com.example.pet_universe.databinding.FragmentLivePetsBinding
import com.example.pet_universe.ui.profile.ProfileViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

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
//        profileViewModel.userInitial.observe(viewLifecycleOwner) { initial ->
//            binding.root.findViewById<TextView>(R.id.profileIcon).text = initial ?: ""
//        }

        // Navigate to AccountSettingsFragment on profileIcon click
//        binding.root.findViewById<TextView>(R.id.profileIcon).setOnClickListener {
//            findNavController().navigate(R.id.action_global_to_accountSettings)
//        }

        // Set up database and ViewModel
        database = ListingDatabase.getInstance(requireContext())
        listingDao = database.listingDao
        repository = ListingRepository(listingDao)
        viewModelFactory = ListingViewModelFactory(repository)
        listingViewModel =
            ViewModelProvider(this, viewModelFactory).get(ListingViewModel::class.java)

        // Set up search functionality
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                livePetsViewModel.setSearchQuery(s?.toString() ?: "")
            }
        })

        // Set up spinner listeners
        binding.typeFilterSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedType = parent?.getItemAtPosition(position).toString()
                    livePetsViewModel.setSelectedPetType(selectedType)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        binding.priceFilterSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedPrice = parent?.getItemAtPosition(position).toString()
                    livePetsViewModel.setSelectedPriceRange(selectedPrice)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        binding.locationFilterSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedLocation = parent?.getItemAtPosition(position).toString()
                    livePetsViewModel.setSelectedLocation(selectedLocation)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        // Initialize RecyclerView with an empty list initially
        binding.petRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val petAdapter = PetAdapter(listOf()) { pet ->
            livePetsViewModel.selectPet(pet)
            findNavController().navigate(R.id.action_livePetsFragment_to_individualPetFragment)
        }
        binding.petRecyclerView.adapter = petAdapter

        // Observe listings from Firebase and update adapter
        listingViewModel.fetchListingsFromFirebase()
        observeListingsFromRoom()

        // Observe filtered pets instead of direct listings
        livePetsViewModel.filteredPets.observe(viewLifecycleOwner) { filteredPets ->
            petAdapter.updatePets(filteredPets)
        }

        // Update the existing listings observer
        listingViewModel.allListingsLiveData.observe(viewLifecycleOwner) { listings ->
            val pets = listings.map { listing ->
                Pet(
                    name = listing.title,
                    price = listing.price,
                    type = listing.type,
                    description = listing.description,
                    imageUrls = listing.imageUrls,
                    petLocation = listing.meetingLocation
                )
            }
            livePetsViewModel.updatePets(pets)  // This will trigger filtering
            saveListingsToLocalDatabase(pets) // TODO
        }
    }

    private fun observeListingsFromRoom() {
        listingViewModel.allListingsLiveData.observe(viewLifecycleOwner) { listings ->
            val pets = listings.map { listing ->
                Pet(
                    name = listing.title,
                    price = listing.price,
                    type = listing.type,
                    description = listing.description,
                    imageUrls = listing.imageUrls,
                    petLocation = listing.meetingLocation
                )
            }
            (binding.petRecyclerView.adapter as PetAdapter).updatePets(pets)
        }
    }


    private fun saveListingsToLocalDatabase(listings: List<Pet>) {
        lifecycleScope.launch {
            listings.forEach { listing ->
//                val existingListing = listingViewModel.getListingById(listing.id)
//                if (existingListing == null) {
//                    listingViewModel.insert(listing)
//                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
