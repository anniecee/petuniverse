package com.example.pet_universe.ui.livePets

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.pet_universe.database.Listing

class LivePetsViewModel(application: Application) : AndroidViewModel(application) {
    private val _userInitial = MutableLiveData<String>()
    val userInitial: LiveData<String> = _userInitial

    private val _selectedPet = MutableLiveData<Pet>()
    val selectedPet: LiveData<Pet> = _selectedPet

    private val _filteredPets = MutableLiveData<List<Pet>>()
    val filteredPets: LiveData<List<Pet>> = _filteredPets

    private val _selectedPetType = MutableLiveData<String>()
    val selectedPetType: LiveData<String> = _selectedPetType

    private val _selectedPriceRange = MutableLiveData<String>()
    val selectedPriceRange: LiveData<String> = _selectedPriceRange

    private val _selectedLocation = MutableLiveData<String>()
    val selectedLocation: LiveData<String> = _selectedLocation

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery

    private var currentPets: List<Pet> = listOf()

    init {
        loadUserInitial()
        // Initialize with default values
        _selectedPetType.value = "All"
        _selectedPriceRange.value = "All"
        _selectedLocation.value = "All"
        _searchQuery.value = ""
    }

    private fun loadUserInitial() {
        val sharedPref = getApplication<Application>().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val initial = sharedPref.getString("user_initial", "") ?: ""
        _userInitial.value = initial
    }

    // Keep the existing selectPet method for individual pet viewing
    fun selectPet(pet: Pet) {
        _selectedPet.value = pet
    }

    fun updatePets(pets: List<Pet>) {
        currentPets = pets
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun setSelectedPetType(type: String) {
        _selectedPetType.value = type
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
        var filteredList = currentPets

        // Apply search filter
        searchQuery.value?.let { query ->
            if (query.isNotEmpty()) {
                filteredList = filteredList.filter { pet ->
                    pet.name.contains(query, ignoreCase = true) ||
                            pet.description.contains(query, ignoreCase = true)
                }
            }
        }

        // Apply pet type filter
        selectedPetType.value?.let { type ->
            if (type != "All") {
                filteredList = filteredList.filter { it.type == type }
            }
        }

        // Apply price filter
        selectedPriceRange.value?.let { range ->
            filteredList = when (range) {
                "\$0 (Adoption)" -> filteredList.filter { it.price < 1 }
                "More than \$0 - \$50" -> filteredList.filter { it.price in 1..50 }
                "\$51 - \$100" -> filteredList.filter { it.price in 51..100 }
                "\$101 - \$200" -> filteredList.filter { it.price in 101..200 }
                "\$201 - \$500" -> filteredList.filter { it.price in 201..500 }
                "More than \$500" -> filteredList.filter { it.price > 500 }
                else -> filteredList
            }
        }

        // Apply location filter
        selectedLocation.value?.let { location ->
            if (location != "All") {
//                 TODO
//                filteredList = filteredList.filter {
//                    it.petLocation.contains(location, ignoreCase = true)
//                }
            }
        }

        _filteredPets.value = filteredList
    }
}