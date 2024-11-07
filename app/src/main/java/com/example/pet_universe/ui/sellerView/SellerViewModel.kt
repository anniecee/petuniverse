package com.example.pet_universe.ui.sellerView

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pet_universe.database.Listing
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

//    var listing: MutableLiveData<List<Listing>> = MutableLiveData()
//
//    fun getListing(): LiveData<List<Listing>> {
//        if (listing.value == null) {
//            FirebaseDatabase.getInstance()
//                //might need to revise this
//                .getReference("listings")
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                        if (dataSnapshot.exists()) {
//                            listing.postValue(toListing(snapshot))
//                        })
//                    }
//
//                    return listing
//                }
//        }
//    }
}
