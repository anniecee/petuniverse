package com.example.pet_universe

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pet_universe.database.UserDao
import com.example.pet_universe.database.UserDatabase
import com.example.pet_universe.database.UserRepository
import com.example.pet_universe.database.UserViewModel
import com.example.pet_universe.database.UserViewModelFactory
import com.example.pet_universe.databinding.ActivityMainBinding
import com.example.pet_universe.ui.accountSettings.SignUpFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Database
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

    // Shared preferences
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Set up Room database for user data
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

        // Set up listeners for buttons
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Check if email and password are not empty
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Launch a coroutine to call the suspend function
                lifecycleScope.launch {
                    handleLogin(email, password)
                }
            } else {
                emailEditText.error = if (email.isEmpty()) "Email required" else null
                passwordEditText.error = if (password.isEmpty()) "Password required" else null
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
    }

    private suspend fun handleLogin(email: String, password: String) {
        // Check if user exists in database
        if (userViewModel.getUserByEmail(email) == null) {
            emailEditText.error = "User not found"
            return
        } else {
            // Check if password matches the user's password
            val user = userViewModel.getUserByEmail(email)
            if (user?.password == password) {
                // Save user data to shared preferences
                sharedPref = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putLong("userId", user.id)
                    putString("email", user.email)
                    apply()
                }

                // Switch to main layout after login
                loadMainLayout()
            } else {
                passwordEditText.error = "Incorrect password"
            }
        }
    }

    private fun loadMainLayout() {
        // Inflate the main activity layout with bottom navigation
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup BottomNavigationView with NavController
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_seller, R.id.navigation_accounts)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        // Set up action to ensure HomeFragment is loaded as default
        binding.navView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }
                R.id.navigation_seller -> {
                    navController.navigate(R.id.navigation_seller)
                    true
                }
                R.id.navigation_accounts -> {
                    navController.navigate(R.id.navigation_accounts)
                    true
                }
                else -> false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Use NavController to handle navigation back to previous fragment
        return findNavController(R.id.nav_host_fragment_activity_main).navigateUp() || super.onSupportNavigateUp()
    }

}
