package com.example.pet_universe.ui.sellerView

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.InputStream
import java.util.UUID
import kotlin.coroutines.resumeWithException

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

    // Firebase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    // Elements
    private lateinit var titleEditText : EditText
    private lateinit var priceEditText : EditText
    private lateinit var descriptionEditText : EditText
    private lateinit var categorySpinner : Spinner
    private lateinit var typeSpinner: Spinner
    private lateinit var locationEditText : EditText
    private lateinit var changePhotoButton : Button
    private lateinit var photoTextView: TextView
    private var imageUrls = mutableListOf<String>()

    // Image
    private var imageUri: Uri? = null

    // User
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_listing_detail)

        // Set up shared preferences
        sharedPref = getSharedPreferences("UserProfile", MODE_PRIVATE)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        // Enable Firestore logging
        // FirebaseFirestore.setLoggingEnabled(true)

        // Set up Room database
        database = ListingDatabase.getInstance(this)
        listingDao = database.listingDao
        repository = ListingRepository(listingDao)
        viewModelFactory = ListingViewModelFactory(repository)
        listingViewModel = ViewModelProvider(this, viewModelFactory).get(ListingViewModel::class.java)

        // User ID
        userId = auth.currentUser?.uid ?: return

        // Get the listing from the database
        val listingId = intent.getLongExtra("listingId", -1)
        lifecycleScope.launch {
            listingViewModel.getListingById(listingId)?.let {
                listing = it
                setListingValues()
            } ?: run {
                // Handle the case where the listing is not found
                Toast.makeText(this@EditListingActivity, "Listing not found", Toast.LENGTH_SHORT).show()
                finish()
            }
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
        typeSpinner = findViewById(R.id.typeSpinner)
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
        val price = priceEditText.text.toString().toInt()
        val description = descriptionEditText.text.toString()
        val category = categorySpinner.selectedItem.toString()
        val type = typeSpinner.selectedItem.toString()
        val location = locationEditText.text.toString()

        // Set values for listing
        listing.title = title
        listing.price = price
        listing.description = description
        listing.category = category
        listing.type = type
        listing.meetingLocation = location

//        // Save photo to Firebase Storage and save URL to room database
//        if (imageUri != null) {
//            uploadImageToFirebaseStorage(imageUri!!)
//
//            // Save image URL to room database
//            listing.imageUrls = imageUrls
//        }

        lifecycleScope.launch {
            // Upload image to Firebase Storage and get the URL
            if (imageUri != null) {
                imageUrls.clear()
                var imageUrl: String? = null
                try {
                    imageUrl = uploadImageToFirebaseStorage(imageUri!!)
                    println("Debug: Successfully uploaded image, URL: $imageUrl")
                } catch (e: Exception) {
                    println("Debug: Exception during image upload: ${e.message}")
                }

                if (imageUrl != null) {
                    imageUrls.add(imageUrl)
                    listing.imageUrls = imageUrls
                } else {
                    println("Debug: Failed to upload image")
                }
            } else {
                println("Debug: imageUri is null")
            }

            try {
                println("Debug: Listing before update: $listing")
                listingViewModel.updateListing(listing)
                updateListingInFirebase(listing)
                Toast.makeText(this@EditListingActivity, "Listing updated", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: Exception) {
                println("Debug: Exception during listing update: ${e.message}")
            }
        }

    }

    private fun updateListingInFirebase(listing: Listing) {
        val listingRef = firestore.collection("users/$userId/listings").document(listing.id.toString())
        val updatedListing = hashMapOf(
            "title" to listing.title,
            "price" to listing.price.toDouble(),
            "description" to listing.description,
            "category" to listing.category,
            "type" to listing.type,
            "meetingLocation" to listing.meetingLocation,
            "imageUrls" to listing.imageUrls
        )

        listingRef.update(updatedListing)
            .addOnSuccessListener {
                println("Listing updated in Firebase")
            }
            .addOnFailureListener { e ->
                println("Error updating listing in Firebase: $e")
            }
    }

//    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
//        val storageRef = storage.reference
//
//        // Set up image reference
//        val imageByteArray = getImageByteArray(this, imageUri)
//        val fileNameWithExtension = getFileName(applicationContext, imageUri)
//        val imageRef = storageRef.child("images/${userId}/${listing.id}/$fileNameWithExtension")
//        println("Uploading to path: ${imageRef.path}")
//
//        // Upload image to Firebase Storage
//        val uploadTask = imageRef.putBytes(imageByteArray!!)
//        uploadTask.continueWithTask { task ->
//            if (!task.isSuccessful) {
//                task.exception?.let { throw it }
//            }
//            imageRef.downloadUrl
//        }.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val downloadUri = task.result.toString()
//                println("Successfully added the downloaded image uri")
//                imageUrls.add(downloadUri)
//            }
//        }.addOnFailureListener() { e ->
//            println("Failed to upload image to Firebase Storage: ${e.message}")
//        }
//    }

    private suspend fun uploadImageToFirebaseStorage(imageUri: Uri): String? {
        return suspendCancellableCoroutine { continuation ->
            val storageRef = storage.reference
            val fileNameWithExtension = getFileName(applicationContext, imageUri)
            val imageRef = storageRef.child("images/${userId}/${listing.id}/$fileNameWithExtension")
            var downloadUri = ""
            println("Debug: Uploading to path: ${imageRef.path}")
            println("Debug: Image URI: $imageUri")

            val uploadTask = imageRef.putFile(imageUri)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    continuation.resume(uri.toString(), null)
                    println("Debug: Successfully added the downloaded image uri: $uri")
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                    println("Debug: Failed to get download URL: ${e.message}")
                }
            }.addOnFailureListener { e ->
                continuation.resumeWithException(e)
                println("Debug: Failed to upload image to Firebase Storage: ${e.message}")
            }.addOnCanceledListener {
                continuation.cancel()
                println("Debug: Upload cancelled")
            }
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

    private fun getImageByteArray(context: Context, uri: Uri): ByteArray? {
        return context.contentResolver.openInputStream(uri)
            ?.use { inputStream: InputStream -> inputStream.readBytes() }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var fileName: String = UUID.randomUUID().toString() // Fallback to a unique name
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    fileName =
                        cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        } else if (uri.path != null) {
            fileName = uri.path!!.substringAfterLast('/')
        }
        return fileName
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}