package com.example.pet_universe.ui.livePets

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class LivePetsViewModel(application: Application) : AndroidViewModel(application) {

    private val _userInitial = MutableLiveData<String>()
    val userInitial: LiveData<String> get() = _userInitial

    private val _selectedPet = MutableLiveData<Pet>()
    val selectedPet: LiveData<Pet> get() = _selectedPet

    private val _selectedPetType = MutableLiveData<String>()
    val selectedPetType: LiveData<String> get() = _selectedPetType

    private val _selectedAgeRange = MutableLiveData<String>()
    val selectedAgeRange: LiveData<String> get() = _selectedAgeRange

    init {
        loadUserInitial()
    }

    // Placeholder for user initial (profile)
    private fun loadUserInitial() {
        val sharedPref = getApplication<Application>().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val initial = sharedPref.getString("user_initial", "") ?: ""
        _userInitial.value = initial
    }

    // Method to set the selected pet
    fun selectPet(pet: Pet) {
        _selectedPet.value = pet
    }

    // Methods to set selected filter options
    fun setSelectedPetType(type: String) {
        _selectedPetType.value = type
    }

    fun setSelectedAgeRange(age: String) {
        _selectedAgeRange.value = age
    }
}
