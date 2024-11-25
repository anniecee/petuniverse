package com.example.pet_universe.ui.accountSettings


import android.content.Intent
import android.os.Bundle

import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.pet_universe.MainActivity
import com.example.pet_universe.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChangeEmailActivity : AppCompatActivity() {
    private lateinit var  emailAddress: EditText
    private lateinit var  saveEmailEntry: Button
    private lateinit var auth:FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_email)

        emailAddress=findViewById(R.id.emailInput)
        saveEmailEntry = findViewById(R.id.saveEmailbutton)

        auth = FirebaseAuth.getInstance()

        //Displaying username (optimize later, perhaps use ProfileViewModel)
        if (currentUserId.isNotEmpty()) {
            firestore.collection("users").document(currentUserId).get()
                .addOnSuccessListener { document ->
                    val email = document.getString("email") ?: ""
                    emailAddress.setText(email)
                }
                .addOnFailureListener {
                    // Handle failure
                }
        }

        saveEmailEntry.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}


