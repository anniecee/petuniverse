package com.example.pet_universe.ui.livePets

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class IndividualPetViewModel(private val livePetsViewModel: LivePetsViewModel) : ViewModel() {

    // Expose selected pet data from LivePetsViewModel
    val selectedPet: LiveData<Pet> = livePetsViewModel.selectedPet
}
