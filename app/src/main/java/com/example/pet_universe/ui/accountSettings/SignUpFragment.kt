package com.example.pet_universe.ui.accountSettings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pet_universe.R
import com.example.pet_universe.database.User
import com.example.pet_universe.database.UserDao
import com.example.pet_universe.database.UserDatabase
import com.example.pet_universe.database.UserRepository
import com.example.pet_universe.database.UserViewModel
import com.example.pet_universe.database.UserViewModelFactory
import com.example.pet_universe.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Database
    private lateinit var database: UserDatabase
    private lateinit var userDao: UserDao
    private lateinit var repository: UserRepository
    private lateinit var viewModelFactory: UserViewModelFactory
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set up Room database for user data
        database = UserDatabase.getInstance(requireContext())
        userDao = database.userDao
        repository = UserRepository(userDao)
        viewModelFactory = UserViewModelFactory(repository)
        userViewModel = ViewModelProvider(this, viewModelFactory).get(UserViewModel::class.java)

        // Initialize the user data
        val user = User()

        binding.saveButton.setOnClickListener {
            val firstName = binding.firstNameEditText.text.toString()
            val lastName = binding.lastNameEditText.text.toString()
            val email = binding.emailEditTextSignUp.text.toString()
            val password = binding.passwordEditTextSignUp.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            // added this for firebase sign up
            if (validateInputs(firstName, lastName, email, password, confirmPassword)) {
                registerUserWithFirebase(firstName, lastName, email, password)
            }

            //Trying a new thing with the Firebase database
//            if (password == confirmPassword && email.isNotEmpty() && password.isNotEmpty()) {
//                // Simulate registration process
//                Toast.makeText(requireContext(), "Registration Complete", Toast.LENGTH_SHORT).show()
//                // TODO: Save user data to database in the future - Done
//                // This is where you would add the code to save the user data to a database.
//                user.firstName = firstName
//                user.lastName = lastName
//                user.email = email
//                user.password = password
//                userViewModel.insert(user)
//
//                parentFragmentManager.popBackStack()
//            } else {
//                if (password != confirmPassword) {
//                    binding.confirmPasswordEditText.error = "Passwords do not match"
//                }
//                if (email.isEmpty()) {
//                    binding.emailEditTextSignUp.error = "Email required"
//                }
//                if (password.isEmpty()) {
//                    binding.passwordEditTextSignUp.error = "Password required"
//                }
//            }

            if (firstName.isNotEmpty()) {
                // Save the first letter of the first name in SharedPreferences to be used to display as a profile icon in the livePetsFragment
                val initial = firstName[0].toString().uppercase()
                val sharedPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("user_initial", initial)
                    apply()
                }
            }
        }

        binding.cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun validateInputs(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (password != confirmPassword) {
            binding.confirmPasswordEditText.error = "Passwords do not match"
            isValid = false
        }
        if (email.isEmpty()) {
            binding.emailEditTextSignUp.error = "Email required"
            isValid = false
        }
        if (password.isEmpty()) {
            binding.passwordEditTextSignUp.error = "Password required"
            isValid = false
        }
        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(context, "Name fields cannot be empty", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        return isValid
    }


    private fun registerUserWithFirebase(firstName: String, lastName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Firebase registration success
                    val userId = auth.currentUser?.uid ?: ""
                    saveUserToFirestoreAndRoom(userId, firstName, lastName, email, password)
                } else {
                    // Handle error if email already exists
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(context, "Email is already registered. Use a different email.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun saveUserToFirestoreAndRoom(userId: String, firstName: String, lastName: String, email: String, password: String) {
        // Create a user map for Firestore
        val userMap = hashMapOf(
            "userId" to userId,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email
        )

        // Add user data to Firestore
        firestore.collection("users").document(userId).set(userMap)
            .addOnSuccessListener {
                Toast.makeText(context, "User registered successfully!", Toast.LENGTH_SHORT).show()
                // Save user to Room database
                saveUserToLocalDatabase(firstName, lastName, email, password)
                parentFragmentManager.popBackStack() // Navigate back
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserToLocalDatabase(firstName: String, lastName: String, email: String, password: String) {
        // Create and insert the user into Room database
        val user = User(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password // Consider encrypting passwords in a real app
        )

        userViewModel.insert(user)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
