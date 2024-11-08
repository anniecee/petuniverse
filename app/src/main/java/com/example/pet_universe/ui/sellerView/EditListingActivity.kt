package com.example.pet_universe.ui.sellerView

import android.app.Activity
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
import androidx.lifecycle.lifecycleScope
import com.example.pet_universe.R
import com.example.pet_universe.database.Listing
import com.example.pet_universe.database.ListingDatabase
import com.example.pet_universe.database.ListingDatabaseDao
import com.example.pet_universe.database.ListingRepository
import com.example.pet_universe.database.ListingViewModel
import com.example.pet_universe.database.ListingViewModelFactory
import kotlinx.coroutines.launch

class EditListingActivity : AppCompatActivity() {
    private lateinit var backButton : Button
    private lateinit var saveButton : Button

    // Shared pref
    private lateinit var sharedPref: SharedPreferences

    // Database
    private lateinit var database: ListingDatabase
    private lateinit var listingDao: ListingDatabaseDao
    private lateinit var repository: ListingRepository
    private lateinit var viewModelFactory: ListingViewModelFactory
    private lateinit var listingViewModel: ListingViewModel
    private lateinit var listing: Listing

    // Elements
    private lateinit var titleEditText : EditText
    private lateinit var priceEditText : EditText
    private lateinit var descriptionEditText : EditText
    private lateinit var categorySpinner : Spinner
    private lateinit var locationEditText : EditText
    private lateinit var changePhotoButton : Button
    private lateinit var photoTextView: TextView

    // Image
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_listing_detail)

        // Set up shared preferences
        sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)

        // Set up database
        database = ListingDatabase.getInstance(this)
        listingDao = database.listingDao
        repository = ListingRepository(listingDao)
        viewModelFactory = ListingViewModelFactory(repository)
        listingViewModel = ViewModelProvider(this, viewModelFactory).get(ListingViewModel::class.java)

        // Get the listing from the database
        val listingId = intent.getLongExtra("listingId", -1)
        lifecycleScope.launch {
            listing = listingViewModel.getListingById(listingId)
            setListingValues()
        }

        // Set on click listener for the change photo button
        changePhotoButton = findViewById(R.id.changePhotoButton)
        changePhotoButton.setOnClickListener {
            openGalleryForImage()
        }

        // Set on click listener for the back button
        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Set on click listener for the save button
        saveButton = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            saveListing()
            finish()
        }
    }

    private fun setListingValues() {
        // Set the text fields to the listing's values
        titleEditText = findViewById(R.id.titleEditText)
        priceEditText = findViewById(R.id.priceEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        locationEditText = findViewById(R.id.meetingLocationEditText)

        titleEditText.setText(listing.title)
        priceEditText.setText(listing.price.toString())
        descriptionEditText.setText(listing.description)
        locationEditText.setText(listing.meetingLocation)
        categorySpinner.setSelection(when (listing.category) {
            "Dog" -> 0
            "Cat" -> 1
            "Bird" -> 2
            "Pet Food" -> 3
            "Accessories" -> 4
            "Other" -> 5
            else -> 0
        })
    }

    private fun saveListing() {
        // Get input values
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
            val inputStream = contentResolver.openInputStream(imageUri!!)
            val photo = inputStream?.readBytes()
            if (photo != null) {
                listing.photo = photo
            }
        }

        // Update the listing in the database
        lifecycleScope.launch {
            listingViewModel.updateListing(listing)
        }

        Toast.makeText(this, "Listing updated", Toast.LENGTH_SHORT).show()
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
    }
}