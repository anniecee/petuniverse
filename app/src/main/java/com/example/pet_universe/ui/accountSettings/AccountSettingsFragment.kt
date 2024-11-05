package com.example.pet_universe.ui.accountSettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pet_universe.databinding.FragmentAccountsBinding

class AccountSettingsFragment : Fragment() {

    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!
    private lateinit var accountsViewModel: AccountsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        accountsViewModel = ViewModelProvider(this).get(AccountsViewModel::class.java)
        _binding = FragmentAccountsBinding.inflate(inflater, container, false)

        // Observe the sign-in state and update the button text accordingly
        accountsViewModel.isSignedIn.observe(viewLifecycleOwner) { isSignedIn ->
            binding.signInOutButton.text = if (isSignedIn) "Sign Out" else "Sign In"
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle profile click action
        binding.profileTextView.setOnClickListener {
            // Implement navigation or action to profile details
        }

        // Toggle sign-in state when button is clicked
        binding.signInOutButton.setOnClickListener {
            accountsViewModel.toggleSignInState()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }
}
