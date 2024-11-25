package com.example.pet_universe.ui.accountSettings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
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

class AccountSettingsFragment : Fragment() {

    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!
    private lateinit var accountsViewModel: AccountsViewModel
    private lateinit var auth: FirebaseAuth
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        accountsViewModel = ViewModelProvider(this).get(AccountsViewModel::class.java)
        _binding = FragmentAccountsBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        // Displaying username (optimize later, perhaps use ProfileViewModel)
        if (currentUserId.isNotEmpty()) {
            firestore.collection("users").document(currentUserId).get()
                .addOnSuccessListener { document ->
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val email = document.getString("email") ?: ""
                    binding.userNameTextView.text = "$firstName $lastName"
                    binding.useremailFromDB.text = "$email"
                }
                .addOnFailureListener {
                    // Handle failure
                }
        }

        binding.signInOutButton.text = "Sign Out"

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up profile icon initial
        profileViewModel.userInitial.observe(viewLifecycleOwner) { initial ->
            val profileIconLayout = binding.root.findViewById<RelativeLayout>(R.id.profileIcon)
            val profileTextView = profileIconLayout.findViewById<TextView>(R.id.profileTextView)
            profileTextView.text = initial ?: "You"
        }

        binding.editName.setOnClickListener {
            if (auth.currentUser != null) {
                // Show confirmation dialog before sign-out
                startActivity(Intent(requireContext(), ChangeNameActivity::class.java))
                requireActivity().finish()
            } else {
                navigateToSignIn() // Navigate to sign-in if user is not signed in
            }
        }

        binding.editEmail.setOnClickListener {
            if (auth.currentUser != null) {
                // Show confirmation dialog before sign-out
                startActivity(Intent(requireContext(), ChangeEmailActivity::class.java))
                requireActivity().finish()
            } else {
                navigateToSignIn() // Navigate to sign-in if user is not signed in
            }
        }

        binding.signInOutButton.setOnClickListener {
            // Handle sign-out or sign-in toggle
            if (auth.currentUser != null) {
                // Show confirmation dialog before sign-out
                AlertDialog.Builder(requireContext())
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton("Yes") { _, _ ->
                        auth.signOut()  // Sign the user out
                        accountsViewModel.isSignedIn = MutableLiveData<Boolean>().apply {
                            value = false // Update the sign-in state
                        }
                        navigateToSignIn()  // Redirect to sign-in
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                navigateToSignIn() // Navigate to sign-in if user is not signed in
            }
        }
    }

    private fun navigateToSignIn() {
        // Navigate the user to the sign-in screen
        startActivity(Intent(requireContext(), SignInActivity::class.java))
        requireActivity().finish()  // Optionally close the current activity
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }
}
