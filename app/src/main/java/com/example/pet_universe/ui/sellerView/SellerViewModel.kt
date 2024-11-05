package com.example.pet_universe.ui.sellerView

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SellerViewModel : ViewModel() {

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> = _imageUri

    private val _description = MutableLiveData<String>()
    val description: LiveData<String> = _description

    // Set the image URI (e.g., when the user selects an image)
    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    // Set the description text
    fun setDescription(text: String) {
        _description.value = text
    }
}
