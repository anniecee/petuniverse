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
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.UUID
import kotlinx.coroutines.tasks.await

class AddListingActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    // Initialization for database
    private lateinit var database: ListingDatabase
    private lateinit var listingDao: ListingDatabaseDao
    private lateinit var repository: ListingRepository
    private lateinit var viewModelFactory: ListingViewModelFactory
    private lateinit var listingViewModel: ListingViewModel

    // Image
    private var imageUri: Uri? = null
    private var imageUrl: String = ""

    // Elements
    private lateinit var titleEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var typeSpinner: Spinner
    private lateinit var locationEditText: EditText
    private lateinit var uploadPhotoButton: Button
    private lateinit var sellButton: Button
    private lateinit var cancelButton: Button
    private lateinit var photoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_listing)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true)

        // Initialize Room database
        database = ListingDatabase.getInstance(this)
        listingDao = database.listingDao
        repository = ListingRepository(listingDao)
        viewModelFactory = ListingViewModelFactory(repository)
        listingViewModel =
            ViewModelProvider(this, viewModelFactory).get(ListingViewModel::class.java)

        // Initialize UI components
        titleEditText = findViewById(R.id.titleEditText)
        priceEditText = findViewById(R.id.priceEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        typeSpinner = findViewById(R.id.typeSpinner)
        locationEditText = findViewById(R.id.meetingLocationEditText)
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton)
        sellButton = findViewById(R.id.sellButton)
        photoTextView = findViewById(R.id.photoTextView)

        // Set click listeners
        uploadPhotoButton.setOnClickListener { openGalleryForImage() }
        sellButton.setOnClickListener {
            lifecycleScope.launch {
                if (titleEditText.text.isNullOrEmpty() || priceEditText.text.isNullOrEmpty() || descriptionEditText.text.isNullOrEmpty() || locationEditText.text.isNullOrEmpty()) {
                    Toast.makeText(
                        applicationContext,
                        "Please fill in all required fields.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    saveListing()
                    finish()
                }
            }
        }
        cancelButton = findViewById(R.id.cancelButton)
        cancelButton.setOnClickListener {
            finish()
        }
    }

    // Launch gallery to pick an image
    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    // Handle image result
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                imageUri = data?.data
                val uri = imageUri // This to prevent null pointer exception
                if (uri != null) {
                    val fileName = uri.pathSegments.last()
                    photoTextView = findViewById(R.id.photoTextView)
                    photoTextView.text = fileName.toString() + ".jpg"
                    Toast.makeText(this, "Image uploaded successfully.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    // Save listing to Firestore and Room
    private suspend fun saveListing() {
        val uniqueId = System.currentTimeMillis() * 1000 + (0..999).random()
        var imageByteArray: ByteArray? = null
        if (imageUri != null) {
            imageByteArray = getImageByteArray(this, imageUri!!)
        }

        val listing = Listing(
            id = uniqueId,
            title = titleEditText.text.toString(),
            price = priceEditText.text.toString().toInt(),
            description = descriptionEditText.text.toString(),
            category = categorySpinner.selectedItem.toString(),
            type = typeSpinner.selectedItem.toString(),
            meetingLocation = locationEditText.text.toString(),
            sellerId = auth.currentUser?.uid
        )

        val success = saveListingToFirestore(listing, imageByteArray)
        if (success) {
            listing.imageUrl = imageUrl
            listingViewModel.insert(listing) // Save listing to Room
            Toast.makeText(
                this@AddListingActivity,
                "Listing created successfully!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(this, "Failed to create listing.", Toast.LENGTH_SHORT).show()
        }
    }

    // Save listing to Firestore: both user's collection and global collection
    private suspend fun saveListingToFirestore(
        listing: Listing,
        imageByteArray: ByteArray?
    ): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val userListingRef = firestore.collection("users").document(userId).collection("listings")
            .document(listing.id.toString())
        val globalListingRef = firestore.collection("listings").document(listing.id.toString())

        return try {
            if (imageByteArray == null || imageByteArray.isEmpty()) {
                saveListingDocument(userListingRef, listing, "").await()
                saveListingDocument(globalListingRef, listing, "").await()
            } else {
                imageUrl = uploadImagesToFirebaseStorage(
                    imageByteArray,
                    listing.id.toString(),
                    imageUri!!
                ).await()
                saveListingDocument(userListingRef, listing, imageUrl).await()
                saveListingDocument(globalListingRef, listing, imageUrl).await()
            }
            true
        } catch (e: Exception) {
            println("Failed to save listing to Firestore: ${e.message}")
            false
        }
    }

    // Save listing document to Firestore
    private fun saveListingDocument(
        firestoreRef: DocumentReference,
        listing: Listing,
        imageUrl: String
    ): Task<Void> {
        val listingMap = hashMapOf(
            "id" to listing.id,
            "title" to listing.title,
            "description" to listing.description,
            "price" to listing.price,
            "category" to listing.category,
            "type" to listing.type,
            "meetingLocation" to listing.meetingLocation,
            "sellerId" to listing.sellerId,
            "imageUrl" to imageUrl
        )
        return firestoreRef.set(listingMap)
    }

    // Upload image to Firebase Storage
    private fun uploadImagesToFirebaseStorage(
        imageByteArray: ByteArray,
        listingId: String,
        imageUri: Uri
    ): Task<String> {
        val userId =
            auth.currentUser?.uid ?: return Tasks.forException(Exception("User not authenticated"))
        val storageRef = storage.reference
        val fileNameWithExtension = getFileName(applicationContext, imageUri)
        val imageRef = storageRef.child("images/$userId/$listingId/$fileNameWithExtension")

        return imageRef.putBytes(imageByteArray).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            imageRef.downloadUrl
        }.continueWith { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            task.result.toString()
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