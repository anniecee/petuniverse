package com.example.pet_universe.ui.sellerView


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pet_universe.database.Listing
import com.example.pet_universe.database.ListingDatabase
import com.example.pet_universe.database.ListingDatabaseDao
import com.example.pet_universe.database.ListingRepository
import com.example.pet_universe.database.ListingViewModel
import com.example.pet_universe.database.ListingViewModelFactory
import com.example.pet_universe.databinding.FragmentSellerBinding
import java.io.File

class SellerViewFragment : Fragment() {

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

    private var _binding: FragmentSellerBinding? = null
    private val binding get() = _binding!!
    private lateinit var sellerViewModel: SellerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sellerViewModel = ViewModelProvider(this).get(SellerViewModel::class.java)
        _binding = FragmentSellerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up shared preferences
        sharedPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

        // Set up database
        database = ListingDatabase.getInstance(requireContext())
        listingDao = database.listingDao
        repository = ListingRepository(listingDao)
        viewModelFactory = ListingViewModelFactory(repository)
        listingViewModel = ViewModelProvider(this, viewModelFactory).get(ListingViewModel::class.java)

        // Initialize listing object
        listing = Listing()
        
        // Set sellerId from shared preferences
        listing.sellerId = sharedPref.getLong("userId", -1)

        // Set click listener for upload photo button
        binding.uploadPhotoButton.setOnClickListener {
            openGalleryForImage()
        }

        // Set click listener for sell button
        binding.sellButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val price = binding.priceEditText.text.toString().toDouble()
            val description = binding.descriptionEditText.text.toString()
            val category = binding.categorySpinner.selectedItem.toString()
            val location = binding.meetingLocationEditText.text.toString()

            // Set listing object
            listing.title = title
            listing.price = price
            listing.description = description
            listing.category = category
            listing.meetingLocation = location

            // Save uploaded photo to room database
            if (imageUri != null) {
                // Open input stream to read image
                val inputStream = requireContext().contentResolver.openInputStream(imageUri!!)
                if (inputStream != null) {
                    listing.photo = inputStream.readBytes()
                }
                inputStream?.close()
            }

            println("Listing: $listing")

            // Close fragment
            requireActivity().supportFragmentManager.popBackStack()
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
                sellerViewModel.setImageUri(uri)

                val fileName = uri.pathSegments.last()
                binding.photoTextView.text = fileName.toString() + ".jpg"
                Toast.makeText(requireContext(), "Image uploaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
