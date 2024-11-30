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

    // For more robust search
    private fun calculateRelevanceScore(
        searchWords: List<String>,
        searchContexts: List<String>,
        listingPrice: Int
    ): Int {
        var score = 0

        searchWords.forEach { word ->
            searchContexts.forEachIndexed { index, context ->
                when {
                    // Exact whole word match
                    context.split("\\s+".toRegex()).contains(word) ->
                        score += when (index) {
                            0 -> 3 // Highest score for title matches
                            1 -> 2 // Medium score for description
                            2 -> 1 // Low score for type
                            3 -> 0 // Minimal score for price
                            4 -> 2 // Medium score for location
                            else -> 0
                        }

                    // Partial word match
                    context.contains(word) ->
                        score += when (index) {
                            0 -> 2
                            1 -> 1
                            2 -> 0
                            3 -> 0
                            4 -> 1
                            else -> 0
                        }
                }
            }

            // Special price-related search handling
            word.replace("$", "").toIntOrNull()?.let { searchPrice ->
                // Prioritize exact or very close price matches
                val priceDifference = Math.abs(listingPrice - searchPrice)
                score += when {
                    priceDifference == 0 -> 5 // Exact price match
                    priceDifference <= searchPrice * 0.1 -> 3 // Within 10%
                    priceDifference <= searchPrice * 0.2 -> 1 // Within 20%
                    else -> 0
                }
            }

            // Price range keywords
            when (word) {
                "free" -> if (listingPrice < 1) score += 2
                "cheap" -> if (listingPrice < 50) score += 1
                "expensive" -> if (listingPrice > 200) score += 1
            }
        }

        return score
    }

    private fun applyFilters() {
        var filteredList = currentListings

        // Search filter
        _searchQuery.value?.let { query ->
            if (query.isNotEmpty()) {
                // Enhanced tokenization to handle price-related searches
                val searchWords = query.lowercase()
                    .replace("[^a-zA-Z0-9\\s$]".toRegex(), "")
                    .split("\\s+".toRegex())
                    .filter { it.isNotEmpty() } // Filter out empty strings

                // Only apply search if there are non-empty search words
                if (searchWords.isNotEmpty()) {
                    filteredList = filteredList.mapNotNull { listing ->
                        // Prepare searchable fields, including location
                        val searchContext = listOf(
                            listing.title.lowercase(),
                            listing.description.lowercase(),
                            listing.type.lowercase(),
                            listing.price.toString(),
                            listing.meetingLocation.lowercase()
                        )

                        // Calculate relevance score
                        val relevanceScore = calculateRelevanceScore(
                            searchWords,
                            searchContext,
                            listing.price
                        )

                        // Only return listings with some match
                        if (relevanceScore > 0) {
                            Pair(listing, relevanceScore)
                        } else null
                    }.map { it.first } // Extract the listing
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