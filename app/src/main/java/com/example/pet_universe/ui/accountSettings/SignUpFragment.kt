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
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

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
            binding.confirmPasswordEditText.error = "The passwords you entered do not match. Please check again."
            isValid = false
        }
        if (email.isEmpty()) {
            binding.emailEditTextSignUp.error = "Please enter your email address."
            isValid = false
        }
        if (password.isEmpty()) {
            binding.passwordEditTextSignUp.error = "Please enter a password."
            isValid = false
        }
        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(context, "Please enter your first and last name.", Toast.LENGTH_SHORT).show()
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
                    // Save first initial in SharedPreferences for immediate use
                    if (firstName.isNotEmpty()) {
                        val initial = firstName[0].toString().uppercase()
                        saveInitialToSharedPreferences(initial)
                    }
                } else {
                    // Handle error if email already exists
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(context, "An account with this email already exists. Did you mean to log in?", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Something went wrong: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    // Encrypt password
    fun encryptPassword(password: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encryptedPassword = cipher.doFinal(password.toByteArray(Charsets.UTF_8))

        // Combine IV and encrypted password and encode
        val ivAndEncryptedPassword = iv + encryptedPassword
        return Base64.encodeToString(ivAndEncryptedPassword, Base64.DEFAULT)
    }

    // Generate or retrieve a SecretKey from Android Keystore
    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        return if (keyStore.containsAlias("encryption_key")) {
            (keyStore.getEntry("encryption_key", null) as KeyStore.SecretKeyEntry).secretKey
        } else {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGenerator.init(
                KeyGenParameterSpec.Builder("encryption_key", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            keyGenerator.generateKey()
        }
    }

    private fun saveUserToFirestoreAndRoom(userId: String, firstName: String, lastName: String, email: String, password: String) {
        // Encrypt the password before saving
        val encryptedPassword = encryptPassword(password)

        // Create a user map for Firestore, including the encrypted password
        val userMap = hashMapOf(
            "userId" to userId,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "encryptedPassword" to encryptedPassword // Store the encrypted password
        )

        // Add user data to Firestore
        firestore.collection("users").document(userId).set(userMap)
            .addOnSuccessListener {
                Toast.makeText(context, "Your account has been successfully created!", Toast.LENGTH_SHORT).show()
                // Save user to Room database without storing the password in plain text
                saveUserToLocalDatabase(firstName, lastName, email, encryptedPassword)
                parentFragmentManager.popBackStack() // Navigate back
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Something went wrong: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserToLocalDatabase(firstName: String, lastName: String, email: String, password: String) {
        // Create and insert the user into Room database
        val user = User(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password
        )

        userViewModel.insert(user)
    }

    private fun saveInitialToSharedPreferences(initial: String) {
        val sharedPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_initial", initial)
            apply()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
