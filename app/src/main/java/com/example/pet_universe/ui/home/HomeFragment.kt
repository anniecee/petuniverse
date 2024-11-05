package com.example.pet_universe.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pet_universe.R
import com.example.pet_universe.databinding.FragmentHomeBinding
import com.example.pet_universe.ui.profile.ProfileViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupImageViewListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe user initial from ProfileViewModel and set it in the profile icon
        profileViewModel.userInitial.observe(viewLifecycleOwner) { initial ->
            binding.root.findViewById<TextView>(R.id.profileIcon).text = initial
        }

        // Navigate to AccountSettingsFragment on profileIcon click
        binding.root.findViewById<TextView>(R.id.profileIcon).setOnClickListener {
            findNavController().navigate(R.id.action_global_to_accountSettings)
        }
    }

    private fun setupImageViewListeners() {
        binding.livePetsImageView.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_livePetsFragment)
        }

        binding.foodImageView.setOnClickListener {
            findNavController().navigate(R.id.navigation_seller) // Update with actual destination
        }

        binding.accessoriesImageView.setOnClickListener {
            findNavController().navigate(R.id.navigation_accounts) // Update with actual destination
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
