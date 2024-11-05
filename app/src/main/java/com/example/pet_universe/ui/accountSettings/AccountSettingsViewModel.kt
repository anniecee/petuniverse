package com.example.pet_universe.ui.accountSettings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AccountsViewModel : ViewModel() {

    // LiveData for the sign-in state
    private val _isSignedIn = MutableLiveData<Boolean>().apply {
        value = false // Assume the user is initially signed out
    }
    val isSignedIn: LiveData<Boolean> = _isSignedIn

    // Function to toggle sign-in state
    fun toggleSignInState() {
        _isSignedIn.value = _isSignedIn.value?.not()
    }
}
