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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

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

    // Shared preferences for user data (without password)
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

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

        // Set up listeners for login button
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Use Firebase to handle login
                handleFirebaseLogin(email, password)
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
                        // Switch to main layout
                        loadMainLayout()
                    }
                } else {
                    // Sign-in failed
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loadMainLayout() {
        // Ensure binding is set up at the class level
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
        return findNavController(R.id.nav_host_fragment_activity_main).navigateUp() || super.onSupportNavigateUp()
    }
}

