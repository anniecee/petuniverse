package com.example.pet_universe.ui.accountSettings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.pet_universe.databinding.FragmentAccountsBinding
import com.example.pet_universe.ui.profile.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChangeNameActivity : AppCompatActivity() {
    private lateinit var  fName: EditText
    private lateinit var  lName: EditText
    private lateinit var  saveEntry: Button
    private lateinit var auth:FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_change_name)

        fName=findViewById(R.id.nameF)
        lName = findViewById(R.id.nameL)
        saveEntry = findViewById(R.id.button)

        auth = FirebaseAuth.getInstance()

         //Displaying username (optimize later, perhaps use ProfileViewModel)
        if (currentUserId.isNotEmpty()) {
            firestore.collection("users").document(currentUserId).get()
                .addOnSuccessListener { document ->
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    fName.setText(firstName)
                    lName.setText(lastName)
                }
                .addOnFailureListener {
                    // Handle failure
                }
        }

        saveEntry.setOnClickListener {
//            val sPassword = setPassword.text.toString()
//            if (validateInputs(sPassword)) {
//                auth.sendPasswordResetEmail(sPassword)
//                    .addOnSuccessListener {
//                        Toast.makeText(this, "Please check your email", Toast.LENGTH_SHORT).show()
//                        finish()
//                    }
//                    .addOnFailureListener {
//                        Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
//                    }
//            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}


