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
import com.example.pet_universe.database.Converters
import com.example.pet_universe.database.Listing
import com.example.pet_universe.database.ListingDatabase
import com.example.pet_universe.database.ListingDatabaseDao
import com.example.pet_universe.database.ListingRepository
import com.example.pet_universe.database.ListingViewModel
import com.example.pet_universe.database.ListingViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.UUID

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
    private lateinit var listing: Listing

    // Shared preferences
    private lateinit var sharedPref: SharedPreferences

    // Image
    private var imageUri: Uri? = null
    // if the user decides to upload multiple images
    private var imageUris: MutableList<Uri> = mutableListOf()
    private var imageUrls = mutableListOf<String>()

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
        listingViewModel = ViewModelProvider(this, viewModelFactory).get(ListingViewModel::class.java)

        // Initialize UI components
        titleEditText = findViewById(R.id.titleEditText)
        priceEditText = findViewById(R.id.priceEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        locationEditText = findViewById(R.id.meetingLocationEditText)
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton)
        sellButton = findViewById(R.id.sellButton)
        photoTextView = findViewById(R.id.photoTextView)

        // Set click listeners
        uploadPhotoButton.setOnClickListener { openGalleryForImage() }
        sellButton.setOnClickListener {
            saveListing()
            finish()
        }
    }

    // Launch gallery to pick an image
    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        resultLauncher.launch(intent)
    }

    // Handle image result
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data

            //new code implementation for selecting multiple images
            imageUris.clear()
            data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    clipData.getItemAt(i)?.uri?.let { imageUris.add(it) }
                }
            } ?: data?.data?.let { imageUris.add(it) }

            // Print out image uris
            imageUris.forEach { uri -> println("Image URI: $uri") }
            photoTextView.text = "${imageUris.size} images selected"
            Toast.makeText(this, "${imageUris.size} images selected", Toast.LENGTH_SHORT).show()
        }
    }

    //new code implementation for firebase and firestore

    private fun saveListing() {
        if (titleEditText.text.isNullOrEmpty() || priceEditText.text.isNullOrEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
            return
        }
        val uniqueId = System.currentTimeMillis() * 1000 + (0..999).random()
        val imagesByteArray = imageUris.mapNotNull { uri -> getImageByteArray(this, uri) }

        // Add debug statement for tracking
        println("Attempting to save listing with ID: $uniqueId")

        val listing = Listing(
            id = uniqueId,
            title = titleEditText.text.toString(),
            price = priceEditText.text.toString().toDouble(),
            description = descriptionEditText.text.toString(),
            category = categorySpinner.selectedItem.toString(),
            meetingLocation = locationEditText.text.toString(),
            sellerId = auth.currentUser?.uid
        )

        saveListingToFirestore(listing, imagesByteArray) { success ->
            if (success) {
                listing.imageUrls = imageUrls // Set imageUrls of listing to the Firestore URLs
                lifecycleScope.launch {
                    listingViewModel.insert(listing) // Save to Room on success
                    println("Listing saved to Room successfully!")
                    println("Listing in Room: $listing")
                    Toast.makeText(this@AddListingActivity, "Listing saved successfully!", Toast.LENGTH_SHORT).show()
                    delay(3000) // Delay for 3 seconds to allow Firestore to save before finishing activity
                    finish()
                }
            } else {
                println("Failed to save listing to Room.")
                Toast.makeText(this, "Failed to save listing.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveListingToFirestore(listing: Listing, imagesByteArray: List<ByteArray>, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val firestoreRef = firestore.collection("users").document(userId).collection("listings").document(listing.id.toString())

        if (imagesByteArray.isEmpty()) {
            // If no images, continue with empty imageUrls list
            saveListingDocument(firestoreRef, listing, emptyList(), onComplete)
        } else {
            // Otherwise, upload images to Firebase Storage
            uploadImagesToFirebaseStorage(imagesByteArray, listing.id.toString()) { imageUrls ->
                saveListingDocument(firestoreRef, listing, imageUrls, onComplete)
            }
        }
    }

    private fun saveListingDocument(firestoreRef: DocumentReference, listing: Listing, imageUrls: List<String>, onComplete: (Boolean) -> Unit) {
        val sellerId = auth.currentUser?.uid ?: return  // Ensure sellerId is set
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // User is authenticated; proceed with Firestore operations
            val listingMap = hashMapOf(
                "id" to listing.id.toString().toLong(),
                "title" to listing.title,
                "description" to listing.description,
                "price" to listing.price.toString().toDouble(),
                "category" to listing.category,
                "meetingLocation" to listing.meetingLocation,
                "sellerId" to sellerId,
                "imageUrls" to imageUrls
            )

            println("sellerId: $sellerId")
            firestoreRef.set(listingMap)
                .addOnSuccessListener {
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    onComplete(false)
                    println("Failed to save listing to Firestore: ${e.message}")
                }
        } else {
            // Prompt the user to sign in
            println("FirestoreError: User is not authenticated.")
        }
    }

    private fun uploadImagesToFirebaseStorage(imagesByteArray: List<ByteArray>, listingId: String, onSuccess: (List<String>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference

        imagesByteArray.forEachIndexed { index, imageByteArray ->
            //val imageRef = storageRef.child("images/$userId/$listingId/image_$index.jpg")
            val fileNameWithExtension = getFileName(applicationContext, imageUris[index])
            val imageRef = storageRef.child("images/$userId/$listingId/$fileNameWithExtension")
            println("Uploading to path: ${imageRef.path}")

            if (imageByteArray.isNotEmpty()) {
                val uploadTask = imageRef.putBytes(imageByteArray)
                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    imageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result.toString()
                        println("Successfully added the downloaded image uri")
                        imageUrls.add(downloadUri)
                        if (imageUrls.size == imagesByteArray.size) {
                            onSuccess(imageUrls)
                        }
                    }
                }.addOnFailureListener() { e ->
                    println("Failed to upload image to Firebase Storage: ${e.message}")
                }
            } else {
                println("Error: Empty imageByteArray for image $index")
            }
        }
    }

    private fun getImageByteArray(context: Context, uri: Uri): ByteArray? {
        return context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream -> inputStream.readBytes() }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var fileName: String = UUID.randomUUID().toString() // Fallback to a unique name
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
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