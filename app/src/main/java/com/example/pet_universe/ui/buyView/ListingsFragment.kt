package com.example.pet_universe.ui.buyView

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pet_universe.R
import com.example.pet_universe.database.ListingDatabase
import com.example.pet_universe.database.ListingDatabaseDao
import com.example.pet_universe.database.ListingRepository
import com.example.pet_universe.database.ListingViewModel
import com.example.pet_universe.database.ListingViewModelFactory
import com.example.pet_universe.databinding.FragmentListingsBinding
import com.google.firebase.firestore.FirebaseFirestore

class ListingsFragment : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var database: ListingDatabase
    private lateinit var listingDao: ListingDatabaseDao
    private lateinit var repository: ListingRepository
    private lateinit var viewModelFactory: ListingViewModelFactory
    private lateinit var listingViewModel: ListingViewModel

    private var _binding: FragmentListingsBinding? = null
    private val binding get() = _binding!!
    private val buyViewModel: BuyViewModel by activityViewModels()
    private val listingsViewModel: ListingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Set up database and ViewModel
        database = ListingDatabase.getInstance(requireContext())
        listingDao = database.listingDao
        repository = ListingRepository(listingDao)
        viewModelFactory = ListingViewModelFactory(repository)
        listingViewModel =
            ViewModelProvider(this, viewModelFactory).get(ListingViewModel::class.java)

        // Category-based listing fetch
        buyViewModel.selectedCategory.observe(viewLifecycleOwner) { category ->
            listingsViewModel.setCurrentCategory(category)
            when (category) {
                "Live Pets" -> listingViewModel.fetchPetListingsFromFirebase()
                "Pet Food" -> listingViewModel.fetchFoodListingsFromFirebase()
                "Pet Accessories" -> listingViewModel.fetchAccessoryListingsFromFirebase()
                "Other" -> listingViewModel.fetchOtherListingsFromFirebase()
            }
        }

        // Set up search functionality
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                listingsViewModel.setSearchQuery(s?.toString() ?: "")
            }
        })

        // Spinner setup for type, price, location filters
        setupSpinnerListeners()

        // RecyclerView setup
        binding.listingsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val listingsAdapter = ListingsAdapter(listOf()) { listing ->
            listingsViewModel.selectListing(listing)
            findNavController().navigate(R.id.action_listingsFragment_to_individualListingFragment)
        }
        binding.listingsRecyclerView.adapter = listingsAdapter

        // Observe and filter listings
        listingViewModel.allListingsLiveData.observe(viewLifecycleOwner) { listings ->
            val filteredListings =
                listings.filter { it.category == buyViewModel.selectedCategory.value }
            listingsViewModel.updateListings(filteredListings)
        }

        // Observe filtered listings for adapter update
        listingsViewModel.filteredListings.observe(viewLifecycleOwner) { filteredListings ->
            listingsAdapter.updateListings(filteredListings)
        }
    }

    private fun setupSpinnerListeners() {
        binding.typeFilterSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedType = parent?.getItemAtPosition(position).toString()
                    listingsViewModel.setSelectedType(selectedType)
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
                    listingsViewModel.setSelectedPriceRange(selectedPrice)
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
                    listingsViewModel.setSelectedLocation(selectedLocation)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}