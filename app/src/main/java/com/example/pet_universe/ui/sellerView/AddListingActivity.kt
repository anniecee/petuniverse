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

    // Elements
    private lateinit var titleEditText : EditText
    private lateinit var priceEditText : EditText
    private lateinit var descriptionEditText : EditText
    private lateinit var categorySpinner : Spinner
    private lateinit var locationEditText : EditText
    private lateinit var uploadPhotoButton : Button
    private lateinit var sellButton : Button
    private lateinit var photoTextView: TextView

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_listing)

        firestore = FirebaseFirestore.getInstance()

        // Set up shared preferences
        sharedPref = this.getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

        // Set up room database
        database = ListingDatabase.getInstance(this)
        listingDao = database.listingDao
        repository = ListingRepository(listingDao)
        viewModelFactory = ListingViewModelFactory(repository)
        listingViewModel = ViewModelProvider(this, viewModelFactory).get(ListingViewModel::class.java)

        // Initialize listing object
        listing = Listing()

        // Set sellerId from shared preferences
        listing.sellerId = sharedPref.getString("userId", null)

        // Set click listener for upload photo button
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton)
        uploadPhotoButton.setOnClickListener {
            openGalleryForImage()
        }

        // Set click listener for sell button
        sellButton = findViewById(R.id.sellButton)
        sellButton.setOnClickListener {

            // Get input values
//            titleEditText = findViewById(R.id.titleEditText)
//            priceEditText = findViewById(R.id.priceEditText)
//            descriptionEditText = findViewById(R.id.descriptionEditText)
//            categorySpinner = findViewById(R.id.categorySpinner)
//            locationEditText = findViewById(R.id.meetingLocationEditText)
//
//            val title = titleEditText.text.toString()
//            val price = priceEditText.text.toString().toDouble()
//            val description = descriptionEditText.text.toString()
//            val category = categorySpinner.selectedItem.toString()
//            val location = locationEditText.text.toString()
//
//            // Set values for listing
//            listing.title = title
//            listing.price = price
//            listing.description = description
//            listing.category = category
//            listing.meetingLocation = location
//
//            // Save uploaded photo to room database
//            if (imageUri != null) {
//                // Open input stream to read image
//                val inputStream = this.contentResolver.openInputStream(imageUri!!)
//                if (inputStream != null) {
//                    listing.photo = inputStream.readBytes()
//                }
//                inputStream?.close()
//            }
//
//            // Print to debug
//            println("Listing: $listing")
//
//            // Save listing to database
//            listingViewModel.insert(listing)
//
//            //save lisitng to the firebase database
////            saveListingToFirebase(listing)
//
//            // Close activity
//            finish()


            // new code from here
            saveListing()
        }
    }
*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_listing)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

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

    //   private fun saveListingToFirebase(listing: Listing) {
//        val converters = Converters()
//
//        // Convert ByteArray photo to List<Int> for Firebase
//        listing.firebasePhoto = converters.fromByteArray(listing.photo)
//
//        firestore.collection("listings")
//            .add(listing)
//            .addOnSuccessListener { Toast.makeText(this, "Listing added", Toast.LENGTH_SHORT).show() }
//            .addOnFailureListener { e -> Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
//    }

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
//            imageUri = data?.data
//            val uri = imageUri // This to prevent null pointer exception
//            if (uri != null) {
//                val fileName = uri.pathSegments.last()
//                photoTextView = findViewById(R.id.photoTextView)
//                photoTextView.text = fileName.toString() + ".jpg"
//                Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show()
//            }

            //new code implementation for selecting multiple images
            imageUris.clear()
            data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    clipData.getItemAt(i)?.uri?.let { imageUris.add(it) }
                }
            } ?: data?.data?.let { imageUris.add(it) }

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
            photo = imagesByteArray.firstOrNull() ?: ByteArray(0),
            sellerId = auth.currentUser?.uid
        )


        saveListingToFirestore(listing, imagesByteArray) { success ->
            if (success) {
                listingViewModel.insert(listing) // Save to Room on success
                Toast.makeText(this, "Listing saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
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
        val listingMap = mapOf(
            "listingId" to listing.id,
            "sellerId" to sellerId,
            "petName" to listing.title,
            "price" to listing.price,
            "location" to listing.meetingLocation,
            "description" to listing.description,
            "imageUrls" to imageUrls
        )

        firestoreRef.set(listingMap)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener { e ->
                onComplete(false)
                println("Failed to save listing to Firestore: ${e.message}")
            }
    }

    private fun uploadImagesToFirebaseStorage(imagesByteArray: List<ByteArray>, listingId: String, onSuccess: (List<String>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference
        val imageUrls = mutableListOf<String>()

        println("Reached in the upload to the firebase section")

        imagesByteArray.forEachIndexed { index, imageByteArray ->
            val imageRef = storageRef.child("images/$userId/$listingId/image_$index.jpg")
            val uploadTask = imageRef.putBytes(imageByteArray)

            println("show me the imageRef, path for image storage $imageRef")
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result.toString()
                    println("Successsfully added the downloaded image uri")
                    imageUrls.add(downloadUri)
                        if (imageUrls.size == imagesByteArray.size) {
                            onSuccess(imageUrls)
                        }
                    }
            }
        }
    }


    private fun getImageByteArray(context: Context, uri: Uri): ByteArray? {
        return context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream -> inputStream.readBytes() }
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}