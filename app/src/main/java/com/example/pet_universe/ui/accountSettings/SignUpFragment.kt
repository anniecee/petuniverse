package com.example.pet_universe.ui.accountSettings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.pet_universe.R
import com.example.pet_universe.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveButton.setOnClickListener {
            val firstName = binding.firstNameEditText.text.toString()
            val lastName = binding.lastNameEditText.text.toString()
            val email = binding.emailEditTextSignUp.text.toString()
            val password = binding.passwordEditTextSignUp.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (password == confirmPassword && email.isNotEmpty() && password.isNotEmpty()) {
                // Simulate registration process
                Toast.makeText(requireContext(), "Registration Complete", Toast.LENGTH_SHORT).show()
                // TODO: Save user data to database in the future
                // This is where you would add the code to save the user data to a database.

                parentFragmentManager.popBackStack()
            } else {
                if (password != confirmPassword) {
                    binding.confirmPasswordEditText.error = "Passwords do not match"
                }
                if (email.isEmpty()) {
                    binding.emailEditTextSignUp.error = "Email required"
                }
                if (password.isEmpty()) {
                    binding.passwordEditTextSignUp.error = "Password required"
                }
            }

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



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
