package com.example.pet_universe

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.pet_universe.database.UserDao
import com.example.pet_universe.database.UserDatabase
import com.example.pet_universe.database.UserRepository
import com.example.pet_universe.database.UserViewModel
import com.example.pet_universe.database.UserViewModelFactory
import com.example.pet_universe.databinding.ActivityMainBinding
import com.example.pet_universe.ui.accountSettings.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    // Database and ViewModel setup
    private lateinit var database: UserDatabase
    private lateinit var userDao: UserDao
    private lateinit var repository: UserRepository
    private lateinit var viewModelFactory: UserViewModelFactory
    private lateinit var userViewModel: UserViewModel

    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Room database and ViewModel
        database = UserDatabase.getInstance(this)
        userDao = database.userDao
        repository = UserRepository(userDao)
        viewModelFactory = UserViewModelFactory(repository)
        userViewModel = ViewModelProvider(this, viewModelFactory).get(UserViewModel::class.java)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            // User is signed in, proceed with loading the main content
            loadMainLayout()

            // Request notification permission for Android 13+
            requestNotificationPermission()
        } else {
            navigateToSignIn()
        }
    }

    private fun loadMainLayout() {
        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment).navController
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_buy, R.id.navigation_seller, R.id.navigation_accounts))
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Set up action to ensure BuyFragment is loaded as default
        binding.navView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_buy -> {
                    navController.navigate(R.id.navigation_buy)
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
                R.id.navigation_chat -> {
                    navController.navigate(R.id.chatListFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun navigateToSignIn() {
        // Navigate the user to the sign-in screen
        startActivity(Intent(this, SignInActivity::class.java))
        finish()  // Optional: Close MainActivity if transitioning to sign-in
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment_activity_main).navigateUp() || super.onSupportNavigateUp()
    }
}

