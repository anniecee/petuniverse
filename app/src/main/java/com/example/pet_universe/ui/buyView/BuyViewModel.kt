package com.example.pet_universe.ui.buyView

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BuyViewModel : ViewModel() {
    val selectedCategory = MutableLiveData<String>()
}