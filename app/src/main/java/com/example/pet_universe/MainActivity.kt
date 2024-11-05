package com.example.pet_universe

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pet_universe.databinding.ActivityMainBinding
import com.example.pet_universe.ui.accountSettings.SignUpFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Initialize views
        val logoImageView: ImageView = findViewById(R.id.logoImageView)
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)
        val signUpTextView: TextView = findViewById(R.id.signUpTextView)

        // Set up listeners for buttons
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Switch to main layout after login
                loadMainLayout()
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
