package com.example.pet_universe.ui.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel(application: Application) : AndroidViewModel(application) {


    private val firestore = FirebaseFirestore.getInstance()
    private val _userInitial = MutableLiveData<String?>()
    val userInitial: LiveData<String?> get() = _userInitial

    init {
        loadUserInitial()
    }

    private fun loadUserInitial() {
        val sharedPref = getApplication<Application>().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        var initial = sharedPref.getString("user_initial", null)

        if (initial != null) {
            _userInitial.value = initial
        } else {
            // Fallback to fetch from Firestore
            fetchInitialFromFirestore()
        }
    }

    private fun fetchInitialFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val firstName = document.getString("firstName") ?: ""
                if (firstName.isNotEmpty()) {
                    val initial = firstName[0].toString().uppercase()
                    _userInitial.value = initial
                    saveInitialToSharedPreferences(initial)
                }
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    private fun saveInitialToSharedPreferences(initial: String) {
        val sharedPref = getApplication<Application>().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        sharedPref.edit().putString("user_initial", initial).apply()
    }

    fun setUserInitial(initial: String) {
        _userInitial.value = initial
        saveInitialToSharedPreferences(initial)
    }
}
