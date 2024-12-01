package com.example.pet_universe.ui.buyView

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pet_universe.R
import com.example.pet_universe.database.Listing
import com.example.pet_universe.database.ListingDatabase
import com.example.pet_universe.database.ListingDatabaseDao
import com.example.pet_universe.database.ListingRepository
import com.example.pet_universe.database.ListingViewModel
import com.example.pet_universe.database.ListingViewModelFactory
import com.example.pet_universe.databinding.FragmentFavoritesBinding
import com.example.pet_universe.databinding.FragmentSellerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class FavoritesFragment: Fragment() {

    // Firestore
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Database
    private lateinit var database: ListingDatabase
    private lateinit var listingDao: ListingDatabaseDao
    private lateinit var repository: ListingRepository
    private lateinit var viewModelFactory: ListingViewModelFactory
    private lateinit var listingViewModel: ListingViewModel
    private val listingsViewModel: ListingsViewModel by activityViewModels()

    // User
    private lateinit var userId: String

    // Recycler View
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: FavoritesAdapter
    private var favoritesList = mutableListOf<Listing>()

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firestore and Firebase Auth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Set up Room database
        database = ListingDatabase.getInstance(requireContext())
        listingDao = database.listingDao
        repository = ListingRepository(listingDao)
        viewModelFactory = ListingViewModelFactory(repository)
        listingViewModel =
            ViewModelProvider(this, viewModelFactory).get(ListingViewModel::class.java)

        // Set up RecyclerView
        recyclerView = binding.favRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerAdapter = FavoritesAdapter(requireContext(), favoritesList, firestore)
        recyclerView.adapter = recyclerAdapter

        userId = auth.currentUser!!.uid

        // Fetch favorites based on internet availability
        if (isInternetAvailable()) {
            fetchFavoritesFromFirebase()
        } else {
            observeListingsFromRoom()
        }

        recyclerAdapter.onItemClick = { listing ->
            listingsViewModel.selectListing(listing)
            findNavController().navigate(R.id.action_favoritesFragment_to_individualListingFragment)
        }
    }

    private fun fetchFavoritesFromFirebase() {
        firestore.collection("users/$userId/favorites")
            .get()
            .addOnSuccessListener { documents ->
                favoritesList.clear()
                for (document in documents) {
                    val listing = document.toObject(Listing::class.java)
                    listing.isFav = true // Mark as favorite for Room since there's no isFav in Firestore's listing
                    favoritesList.add(listing)
                    listingViewModel.insert(listing) // Save to Room database
                }

                recyclerAdapter.notifyDataSetChanged() // Notify adapter of data changes

                // If favoritesList is empty, show empty state view
                if (favoritesList.isEmpty()) {
                    binding.favRecyclerView.visibility = View.GONE
                    binding.emptyStateView.visibility = View.VISIBLE
                } else {
                    binding.favRecyclerView.visibility = View.VISIBLE
                    binding.emptyStateView.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                observeListingsFromRoom() // Fallback to Room database if Firebase fetch fails
            }
    }

    private fun observeListingsFromRoom() {
        lifecycleScope.launch {
            listingViewModel.getFavoritesListings().observe(viewLifecycleOwner) { listings ->
                if (listings.isEmpty()) {
                    binding.favRecyclerView.visibility = View.GONE
                    binding.emptyStateView.visibility = View.VISIBLE
                } else {
                    binding.favRecyclerView.visibility = View.VISIBLE
                    binding.emptyStateView.visibility = View.GONE
                    favoritesList.clear()
                    favoritesList.addAll(listings.filter { it.isFav })
                    recyclerAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    override fun onResume() {
        super.onResume()
        if (isInternetAvailable()) {
            fetchFavoritesFromFirebase()
        } else {
            observeListingsFromRoom()
        }
    }
}