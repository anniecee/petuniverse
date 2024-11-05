package com.example.pet_universe.ui.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val _userInitial = MutableLiveData<String>()
    val userInitial: LiveData<String> get() = _userInitial

    init {
        loadUserInitial()
    }

    private fun loadUserInitial() {
        val sharedPref = getApplication<Application>().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val initial = sharedPref.getString("user_initial", "") ?: ""
        _userInitial.value = initial
    }

    fun setUserInitial(initial: String) {
        _userInitial.value = initial
        val sharedPref = getApplication<Application>().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        sharedPref.edit().putString("user_initial", initial).apply()
    }
}
