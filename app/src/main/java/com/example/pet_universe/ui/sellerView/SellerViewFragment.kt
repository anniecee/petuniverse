package com.example.pet_universe.ui.sellerView


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pet_universe.database.Listing
import com.example.pet_universe.database.ListingDatabase
import com.example.pet_universe.database.ListingDatabaseDao
import com.example.pet_universe.database.ListingRepository
import com.example.pet_universe.database.ListingViewModel
import com.example.pet_universe.database.ListingViewModelFactory
import com.example.pet_universe.databinding.FragmentSellerBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class SellerViewFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore

    // Database
    private lateinit var database: ListingDatabase
    private lateinit var listingDao: ListingDatabaseDao
    private lateinit var repository: ListingRepository
    private lateinit var viewModelFactory: ListingViewModelFactory
    private lateinit var listingViewModel: ListingViewModel

    // Shared preferences
    private lateinit var sharedPref: SharedPreferences

    // Recycler View
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: SellerListingsAdapter
    private var sellerListings = mutableListOf<Listing>()

    private var _binding: FragmentSellerBinding? = null
    private val binding get() = _binding!!
    private lateinit var sellerViewModel: SellerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sellerViewModel = ViewModelProvider(this).get(SellerViewModel::class.java)
        _binding = FragmentSellerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        // Set up shared preferences
        sharedPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

        // Set up database
        database = ListingDatabase.getInstance(requireContext())
        listingDao = database.listingDao
        repository = ListingRepository(listingDao)
        viewModelFactory = ListingViewModelFactory(repository)
        listingViewModel =
            ViewModelProvider(this, viewModelFactory).get(ListingViewModel::class.java)

        // Set up Recycler View
        recyclerView = binding.sellerRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerAdapter =
            SellerListingsAdapter(requireContext(), sellerListings, firestore, sharedPref)
        recyclerView.adapter = recyclerAdapter

        // Find the user's listings & observe data
        val userId = sharedPref.getString("userId", null)

        userId?.let {
            fetchSellerListingsFromFirebase(it)
            observeListingsFromRoom(it)
        }

        // Switch to detail page on item click
        // Learned how to do this from https://www.youtube.com/watch?v=WqrpcWXBz14
        recyclerAdapter.onItemClick = {
            val intent = Intent(requireContext(), EditListingActivity::class.java)
            intent.putExtra("listingId", it.id)
            startActivity(intent)
        }

        // Set click listener for add listing button
        binding.addListingButton.setOnClickListener {
            val intent = Intent(requireContext(), AddListingActivity::class.java)
            startActivity(intent)
        }

    }

    // Fetch listings from Firebase
    private fun fetchSellerListingsFromFirebase(userId: String) {
        firestore.collection("users/$userId/listings")
            .get()
            .addOnSuccessListener { result ->
                val listings = result.mapNotNull { document ->
                    val listing = document.toObject(Listing::class.java)

                    // Check if any listing in sellerListings has the same id as the current listing,
                    // if not, add the listing to sellerListings
                    if (sellerListings.none { it.id == listing.id }) {
                        listing
                    } else {
                        null
                    }
                }

                saveListingsToLocalDatabase(listings)
                println("Fetched Listings from Firebase: ${listings.map { it.id }}")
            }
            .addOnFailureListener { e -> println("Error getting documents: $e") }
    }

    // Save listings to local database
    private fun saveListingsToLocalDatabase(listings: List<Listing>) {
        lifecycleScope.launch {
            listings.forEach { listing ->
                val existingListing =
                    listingViewModel.getListingById(listing.id) // Check if listing exists
                if (existingListing == null) {
                    listingViewModel.insert(listing)
                    sellerListings.add(listing) // Add new listing here to display in recycler view
                }
            }
        }
    }

    private fun observeListingsFromRoom(userId: String) {
        lifecycleScope.launch {
            listingViewModel.getActiveListingsBySellerId(userId)
                .observe(viewLifecycleOwner) { listings ->
                    if (listings.isEmpty()) {
                        binding.sellerRecyclerView.visibility = View.GONE
                        binding.emptyStateView.visibility = View.VISIBLE
                    } else {
                        binding.sellerRecyclerView.visibility = View.VISIBLE
                        binding.emptyStateView.visibility = View.GONE
                        sellerListings.clear()
                        sellerListings.addAll(listings)
                        recyclerAdapter.notifyDataSetChanged()
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = sharedPref.getString("userId", null)
        userId?.let {
            fetchSellerListingsFromFirebase(it)
            observeListingsFromRoom(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
