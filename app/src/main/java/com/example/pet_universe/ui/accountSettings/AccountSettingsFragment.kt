package com.example.pet_universe.ui.accountSettings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.pet_universe.databinding.FragmentAccountsBinding
import com.google.firebase.auth.FirebaseAuth

class AccountSettingsFragment : Fragment() {

    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!
    private lateinit var accountsViewModel: AccountsViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        accountsViewModel = ViewModelProvider(this).get(AccountsViewModel::class.java)
        _binding = FragmentAccountsBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        binding.signInOutButton.text = "Sign Out"
        // Observe the sign-in state
//        accountsViewModel.isSignedIn.observe(viewLifecycleOwner) { isSignedIn ->
//            if (!isSignedIn) {
//                // If not signed in, redirect to sign-in screen
//                navigateToSignIn()
//            }
//            binding.signInOutButton.text = if (isSignedIn) "Sign Out" else "Sign In"
//        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
