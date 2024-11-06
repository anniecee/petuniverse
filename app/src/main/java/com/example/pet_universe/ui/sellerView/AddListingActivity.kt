package com.example.pet_universe.ui.sellerView

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.pet_universe.R
import com.example.pet_universe.database.Listing
import com.example.pet_universe.database.ListingDatabase
import com.example.pet_universe.database.ListingDatabaseDao
import com.example.pet_universe.database.ListingRepository
import com.example.pet_universe.database.ListingViewModel
import com.example.pet_universe.database.ListingViewModelFactory

class AddListingActivity : AppCompatActivity() {

    // Initialization for database
    private lateinit var database: ListingDatabase
    private lateinit var listingDao: ListingDatabaseDao
    private lateinit var repository: ListingRepository
    private lateinit var viewModelFactory: ListingViewModelFactory
    private lateinit var listingViewModel: ListingViewModel
    private lateinit var listing: Listing

    // Shared preferences
    private lateinit var sharedPref: SharedPreferences

    // Image
    private var imageUri: Uri? = null

    // Elements
    private lateinit var titleEditText : EditText
    private lateinit var priceEditText : EditText
    private lateinit var descriptionEditText : EditText
    private lateinit var categorySpinner : Spinner
    private lateinit var locationEditText : EditText
    private lateinit var uploadPhotoButton : Button
    private lateinit var sellButton : Button
    private lateinit var photoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_listing)

        // Set up shared preferences
        sharedPref = this.getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

        // Set up database
        database = ListingDatabase.getInstance(this)
        listingDao = database.listingDao
        repository = ListingRepository(listingDao)
        viewModelFactory = ListingViewModelFactory(repository)
        listingViewModel = ViewModelProvider(this, viewModelFactory).get(ListingViewModel::class.java)

        // Initialize listing object
        listing = Listing()

        // Set sellerId from shared preferences
        listing.sellerId = sharedPref.getLong("userId", -1)

        // Set click listener for upload photo button
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton)
        uploadPhotoButton.setOnClickListener {
            openGalleryForImage()
        }

        // Set click listener for sell button
        sellButton = findViewById(R.id.sellButton)
        sellButton.setOnClickListener {

            // Get input values
            titleEditText = findViewById(R.id.titleEditText)
            priceEditText = findViewById(R.id.priceEditText)
            descriptionEditText = findViewById(R.id.descriptionEditText)
            categorySpinner = findViewById(R.id.categorySpinner)
            locationEditText = findViewById(R.id.meetingLocationEditText)

            val title = titleEditText.text.toString()
            val price = priceEditText.text.toString().toDouble()
            val description = descriptionEditText.text.toString()
            val category = categorySpinner.selectedItem.toString()
            val location = locationEditText.text.toString()

            // Set values for listing
            listing.title = title
            listing.price = price
            listing.description = description
            listing.category = category
            listing.meetingLocation = location

            // Save uploaded photo to room database
            if (imageUri != null) {
                // Open input stream to read image
                val inputStream = this.contentResolver.openInputStream(imageUri!!)
                if (inputStream != null) {
                    listing.photo = inputStream.readBytes()
                }
                inputStream?.close()
            }

            // Print to debug
            println("Listing: $listing")

            // Save listing to database
            listingViewModel.insert(listing)

            // Close activity
            finish()
        }
    }

    // Launch gallery to pick an image
    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    // Handle image result
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            imageUri = data?.data
            val uri = imageUri // This to prevent null pointer exception
            if (uri != null) {
                val fileName = uri.pathSegments.last()
                photoTextView = findViewById(R.id.photoTextView)
                photoTextView.text = fileName.toString() + ".jpg"
                Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        database.close()
    }
}