package com.example.pet_universe.ui.accountSettings

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.pet_universe.R
import com.example.pet_universe.database.User
import com.example.pet_universe.database.UserDao
import com.example.pet_universe.database.UserDatabase
import com.example.pet_universe.database.UserRepository
import com.example.pet_universe.database.UserViewModel
import com.example.pet_universe.database.UserViewModelFactory
import com.example.pet_universe.ui.profile.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileActivity : AppCompatActivity() {

    private val profileViewModel: ProfileViewModel by lazy {
        ViewModelProvider(this).get(ProfileViewModel::class.java)
    }

    // Firebase
    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Room database
    private lateinit var database: UserDatabase
    private lateinit var userDao: UserDao
    private lateinit var repository: UserRepository
    private lateinit var viewModelFactory: UserViewModelFactory
    private lateinit var userViewModel: UserViewModel
    private lateinit var userProfile: User

    // Views
    private lateinit var userNameTV: TextView
    private lateinit var firstNameET: EditText
    private lateinit var lastNameET: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()

        // Set up Room database
        database = UserDatabase.getInstance(this)
        userDao = database.userDao
        repository = UserRepository(userDao)
        viewModelFactory = UserViewModelFactory(repository)
        userViewModel = ViewModelProvider(this, viewModelFactory).get(UserViewModel::class.java)
        userProfile = User()

        // Initialize views
        userNameTV = findViewById(R.id.userNameTextView)
        firstNameET = findViewById(R.id.firstNameEditText)
        lastNameET = findViewById(R.id.lastNameEditText)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)

        // Displaying username
        if (currentUserId.isNotEmpty()) {
            firestore.collection("users").document(currentUserId).get()
                .addOnSuccessListener { document ->
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    userNameTV.text = "$firstName $lastName"
                }
                .addOnFailureListener {
                    // Handle failure
                }
        }

        // Set up profile icon initial
        profileViewModel.userInitial.observe(this) { initial ->
            val profileIconLayout = findViewById<RelativeLayout>(R.id.profileIcon)
            val profileTextView = findViewById<TextView>(R.id.profileTextView)
            profileTextView.text = initial ?: "You"
        }

        // Get user data from Firestore
        firestore.collection("users").document(currentUserId).get()
            .addOnSuccessListener { document ->
                val firstName = document.getString("firstName") ?: ""
                val lastName = document.getString("lastName") ?: ""
                firstNameET.setText(firstName)
                lastNameET.setText(lastName)
            }
            .addOnFailureListener {
                // Handle failure
                println("Failed to get user data")
            }

        // Get user profile from Room database
        lifecycleScope.launch {
            userViewModel.getUserByEmail(auth.currentUser?.email ?: "")?.let { user ->
                userProfile = user
            }
        }

        // Set on click listener for save button
        saveButton.setOnClickListener() {
            lifecycleScope.launch {
                saveProfile()
                finish()
            }
        }

        // Set on click listener for cancel button
        cancelButton.setOnClickListener() {
            finish()
        }
    }

    private suspend fun saveProfile() {

        val firstName = firstNameET.text.toString()
        val lastName = lastNameET.text.toString()

        // Save user profile to Room database
        userProfile.firstName = firstName
        userProfile.lastName = lastName

        println("Debug: $userProfile")

        withContext(Dispatchers.IO) {
            userViewModel.updateProfile(userProfile)
            println("Debug: in room database: ${userViewModel.getUserByEmail(auth.currentUser?.email ?: "")}")
        }

        // Save user profile to Firestore
        withContext(Dispatchers.IO) {
            firestore.collection("users").document(currentUserId)
                .update("firstName", firstName, "lastName", lastName)
                .addOnSuccessListener {
                    // Handle success
                    Toast.makeText(this@EditProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                    println("Debug: in Firestore: $firstName $lastName")
                }
                .addOnFailureListener {
                    // Handle failure
                    Toast.makeText(this@EditProfileActivity, "Profile update failed", Toast.LENGTH_SHORT).show()
                    println("Failed to update user data")
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
