package com.example.pet_universe.ui.accountSettings

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
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
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class ChangePasswordActivity : AppCompatActivity() {

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
    private lateinit var emailET: EditText
    private lateinit var oldPasswordET : EditText
    private lateinit var passwordET: EditText
    private lateinit var saveButton : Button
    private lateinit var cancelButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

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
        emailET = findViewById(R.id.emailEditText)
        oldPasswordET = findViewById(R.id.oldPasswordEditText)
        passwordET = findViewById(R.id.passwordEditText)
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
                val email = document.getString("email") ?: ""
                emailET.setText(email)
                emailET.isEnabled = false // Don't allow user to change email
            }
            .addOnFailureListener {
                // Handle failure
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
                savePassword()
                finish()
            }
        }

        // Set on click listener for cancel button
        cancelButton.setOnClickListener() {
            finish()
        }
    }

    private fun savePassword() {
        val oldPassword = oldPasswordET.text.toString()
        val newPassword = passwordET.text.toString()
        val user = auth.currentUser!!

        if (oldPassword.isNotEmpty() && newPassword.isNotEmpty()) {
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)

            // Prompt the user to re-provide their sign-in credentials
            user.reauthenticate(credential)
                .addOnCompleteListener {
                    println("User re-authenticated.")
                    // Update password in Firebase
                    user.updatePassword(newPassword)
                        .addOnSuccessListener {
                            println("Password updated.")
                            // Update password in Room database
                            userProfile.password = newPassword
                            lifecycleScope.launch {
                                userViewModel.updatePassword(userProfile)
                            }
                        }.addOnFailureListener {
                            // Handle failure
                            println("Failed to update password.")
                        }
                }
        }
    }

}
