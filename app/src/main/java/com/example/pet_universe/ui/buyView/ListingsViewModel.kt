package com.example.pet_universe.ui.buyView

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.pet_universe.database.Listing

class ListingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentCategory = MutableLiveData<String>()
    val currentCategory: LiveData<String> = _currentCategory

    private val _selectedListing = MutableLiveData<Listing>()
    val selectedListing: LiveData<Listing> = _selectedListing

    private val _filteredListings = MutableLiveData<List<Listing>>()
    val filteredListings: LiveData<List<Listing>> = _filteredListings

    private val _selectedType = MutableLiveData<String>()
    private val _selectedPriceRange = MutableLiveData<String>()
    private val _selectedLocation = MutableLiveData<String>()
    private val _searchQuery = MutableLiveData<String>()

    private var currentListings: List<Listing> = listOf()

    init {
        _selectedType.value = "All"
        _selectedPriceRange.value = "All"
        _selectedLocation.value = "All"
        _searchQuery.value = ""
    }

    fun setCurrentCategory(category: String) {
        _currentCategory.value = category
    }

    fun selectListing(listing: Listing) {
        _selectedListing.value = listing
    }

    fun updateListings(listings: List<Listing>) {
        currentListings = listings
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun setSelectedType(type: String) {
        _selectedType.value = type
        applyFilters()
    }

    fun setSelectedPriceRange(priceRange: String) {
        _selectedPriceRange.value = priceRange
        applyFilters()
    }

    fun setSelectedLocation(location: String) {
        _selectedLocation.value = location
        applyFilters()
    }

    private fun applyFilters() {
        var filteredList = currentListings

        // Search filter
        _searchQuery.value?.let { query ->
            if (query.isNotEmpty()) {
                filteredList = filteredList.filter { listing ->
                    listing.title.contains(query, ignoreCase = true) ||
                            listing.description.contains(query, ignoreCase = true)
                }
            }
        }

        // Type filter
        _selectedType.value?.let { type ->
            if (type != "All") {
                filteredList = filteredList.filter { it.type == type }
            }
        }

        // Price filter
        _selectedPriceRange.value?.let { range ->
            filteredList = when (range) {
                "\$0 (Free)" -> filteredList.filter { it.price < 1 }
                "More than \$0 - \$50" -> filteredList.filter { it.price in 1..50 }
                "\$51 - \$100" -> filteredList.filter { it.price in 51..100 }
                "\$101 - \$200" -> filteredList.filter { it.price in 101..200 }
                "\$201 - \$500" -> filteredList.filter { it.price in 201..500 }
                "More than \$500" -> filteredList.filter { it.price > 500 }
                else -> filteredList
            }
        }

        // Location filter (when implemented)
        _selectedLocation.value?.let { location ->
            if (location != "All") {
                // TODO: Implement location filtering
            }
        }

        _filteredListings.value = filteredList
    }
}