package com.example.pet_universe.ui.accountSettings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.example.pet_universe.MainActivity
import com.example.pet_universe.R
import com.example.pet_universe.database.UserDao
import com.example.pet_universe.database.UserDatabase
import com.example.pet_universe.database.UserRepository
import com.example.pet_universe.database.UserViewModel
import com.example.pet_universe.database.UserViewModelFactory
import com.example.pet_universe.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignInBinding

    // Database and ViewModel setup
    private lateinit var database: UserDatabase
    private lateinit var userDao: UserDao
    private lateinit var repository: UserRepository
    private lateinit var viewModelFactory: UserViewModelFactory
    private lateinit var userViewModel: UserViewModel

    // Initialize views
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var logoImageView: ImageView
    private lateinit var loginButton: Button
    private lateinit var signUpTextView: TextView
    private lateinit var resetPasswordTextView: TextView


    // Shared preferences for user data (without password)
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Room database and ViewModel
        database = UserDatabase.getInstance(this)
        userDao = database.userDao
        repository = UserRepository(userDao)
        viewModelFactory = UserViewModelFactory(repository)
        userViewModel = ViewModelProvider(this, viewModelFactory).get(UserViewModel::class.java)

        // Initialize views
        logoImageView = findViewById(R.id.logoImageView)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signUpTextView = findViewById(R.id.signUpTextView)
        resetPasswordTextView = findViewById(R.id.resetPasswordTextView)

        //binding.navView.visibility = View.GONE  // This hides the navigation bar on the SignInActivity

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set up listeners for login button
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Use Firebase to handle login
                handleFirebaseLogin(email, password)
            } else {
                emailEditText.error = if (email.isEmpty()) "Please enter your email address." else null
                passwordEditText.error = if (password.isEmpty()) "Please enter your password." else null
            }
        }

        signUpTextView.setOnClickListener {
            // Load SignUpFragment
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                addToBackStack(null)
                replace(android.R.id.content, SignUpFragment())
            }
        }

        resetPasswordTextView.setOnClickListener {
            // Load ForgetPWFragment
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleFirebaseLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Firebase sign-in successful
                    val user = auth.currentUser
                    if (user != null) {
                        // Store user info in SharedPreferences if needed
                        sharedPref = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("userId", user.uid)
                            putString("email", user.email)
                            apply()
                        }
                        // Start MainActivity after successful login
                        navigateToMainActivity()
                    }
                } else {
                    // Sign-in failed
                    Toast.makeText(baseContext, "Oops! We couldn’t find an account with that email and password. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMainActivity() {
        // Navigate to MainActivity instead of loading the bottom nav in SignInActivity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // Optional: Close SignInActivity if transitioning to MainActivity
    }
}
