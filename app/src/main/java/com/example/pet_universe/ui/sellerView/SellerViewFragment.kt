package com.example.pet_universe.ui.sellerView


import android.app.Activity
import android.content.Intent
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
import com.example.pet_universe.databinding.FragmentSellerBinding

class SellerViewFragment : Fragment() {

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

        // Observe image URI to display selected image
        sellerViewModel.imageUri.observe(viewLifecycleOwner) { uri ->
            binding.petImageView.setImageURI(uri)
        }

        // Observe description and set it in EditText
        sellerViewModel.description.observe(viewLifecycleOwner) { description ->
            binding.petDescriptionEditText.setText(description)
        }

        // Set click listener on ImageView to select an image
        binding.petImageView.setOnClickListener {
            openGalleryForImage()
        }

        // Set click listener for upload button
        binding.uploadButton.setOnClickListener {
            val description = binding.petDescriptionEditText.text.toString()
            if (sellerViewModel.imageUri.value == null || description.isEmpty()) {
                Toast.makeText(requireContext(), "Please add an image and description", Toast.LENGTH_SHORT).show()
            } else {
                sellerViewModel.setDescription(description)
                // TODO: Handle the upload functionality
                Toast.makeText(requireContext(), "Pet uploaded successfully", Toast.LENGTH_SHORT).show()
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
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                sellerViewModel.setImageUri(imageUri)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
