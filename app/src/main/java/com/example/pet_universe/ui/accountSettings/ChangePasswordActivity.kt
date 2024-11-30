package com.example.pet_universe.ui.accountSettings

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ChangePasswordActivity : AppCompatActivity() {

    private val profileViewModel: ProfileViewModel by lazy {
        ViewModelProvider(this).get(ProfileViewModel::class.java)
    }

    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private lateinit var database: UserDatabase
    private lateinit var userDao: UserDao
    private lateinit var repository: UserRepository
    private lateinit var viewModelFactory: UserViewModelFactory
    private lateinit var userViewModel: UserViewModel
    private lateinit var userProfile: User

    // Views
    private lateinit var userNameTV: TextView
    //private lateinit var emailET: EditText
    private lateinit var oldPasswordET: EditText
    private lateinit var passwordET: EditText
    private lateinit var confirmPasswordET: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        auth = FirebaseAuth.getInstance()

        // Initialize Room database and ViewModel
        database = UserDatabase.getInstance(this)
        userDao = database.userDao
        repository = UserRepository(userDao)
        viewModelFactory = UserViewModelFactory(repository)
        userViewModel = ViewModelProvider(this, viewModelFactory).get(UserViewModel::class.java)
        userProfile = User()

        // Initialize views
        userNameTV = findViewById(R.id.userNameTextView)
        //emailET = findViewById(R.id.emailEditText)
        oldPasswordET = findViewById(R.id.oldPasswordEditText)
        passwordET = findViewById(R.id.passwordEditText)
        confirmPasswordET = findViewById(R.id.confirmPasswordEditText)
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
            profileTextView.text = initial ?: "?"
        }

        // Get user data from Firestore
//        firestore.collection("users").document(currentUserId).get()
//            .addOnSuccessListener { document ->
//                val email = document.getString("email") ?: ""
//                emailET.setText(email)
//                emailET.isEnabled = false // Don't allow user to change email
//            }
//            .addOnFailureListener {
//                // Handle failure
//            }

        // Get user profile from Room database
        lifecycleScope.launch {
            userViewModel.getUserByEmail(auth.currentUser?.email ?: "")?.let { user ->
                userProfile = user
            }
        }

        // Set up save button click listener
        saveButton.setOnClickListener {
            val oldPassword = oldPasswordET.text.toString()
            val newPassword = passwordET.text.toString()
            val confirmNewPassword = confirmPasswordET.text.toString()

            // Check if old and new password fields are filled
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Please fill in all required fields.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener  // Prevent further action if fields are empty
            }

            // Additional check for new password requirements (e.g., minimum length)
            if (newPassword.length < 6) {
                Toast.makeText(
                    applicationContext,
                    "New password must be at least 6 characters long.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener  // Prevent further action if new password is too short
            }

            if (confirmNewPassword != newPassword) {
                Toast.makeText(
                    applicationContext,
                    "Password does not match.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener  // Prevent further action if entering password twice fails.
            }
            // If everything is valid, attempt to change the password
            savePassword(oldPassword, newPassword)
        }

        // Set up cancel button click listener
        cancelButton.setOnClickListener {
            finish()  // Close the activity when cancel is clicked
        }
    }

    private fun savePassword(oldPassword: String, newPassword: String) {
        val user = auth.currentUser!!

        // Reauthenticate user with old password
        val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)

        user.reauthenticate(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // If reauthentication is successful, attempt to update password
                    user.updatePassword(newPassword)
                        .addOnSuccessListener {
                            // Password updated successfully
                            Toast.makeText(
                                this,
                                "Password updated successfully.",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Update password in Room database
                            userProfile.password = newPassword
                            lifecycleScope.launch {
                                userViewModel.updatePassword(userProfile)
                            }
                            finish()  // Close activity after successful password update
                        }
                        .addOnFailureListener { e ->
                            // Handle failure when updating password
                            Toast.makeText(
                                this,
                                "Failed to update password: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    // If reauthentication fails (e.g., incorrect old password)
                    Toast.makeText(this, "Old password is incorrect.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
