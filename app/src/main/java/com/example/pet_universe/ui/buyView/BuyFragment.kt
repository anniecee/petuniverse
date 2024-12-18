package com.example.pet_universe.ui.buyView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.pet_universe.R
import com.example.pet_universe.databinding.FragmentBuyBinding
import com.example.pet_universe.ui.profile.ProfileViewModel

class BuyFragment : Fragment() {

    private var _binding: FragmentBuyBinding? = null
    private val binding get() = _binding!!
    private val buyViewModel: BuyViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuyBinding.inflate(inflater, container, false)

        setupImageViewListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe user initial from ProfileViewModel and set it in the profile icon
        profileViewModel.userInitial.observe(viewLifecycleOwner) { initial ->
//            binding.root.findViewById<TextView>(R.id.profileIcon).text = initial ?: ""
        }

        // Navigate to AccountSettingsFragment on profileIcon click
//        binding.root.findViewById<TextView>(R.id.profileIcon).setOnClickListener {
//            findNavController().navigate(R.id.action_global_to_accountSettings)
//        }
    }

    private fun setupImageViewListeners() {
        binding.livePetsImageView.setOnClickListener {
            buyViewModel.selectedCategory.value = "Live Pets"
            findNavController().navigate(R.id.action_navigation_buy_to_listingsFragment)
        }

        binding.foodImageView.setOnClickListener {
            buyViewModel.selectedCategory.value = "Pet Food"
            findNavController().navigate(R.id.action_navigation_buy_to_listingsFragment)
        }

        binding.accessoriesImageView.setOnClickListener {
            buyViewModel.selectedCategory.value = "Pet Accessories"
            findNavController().navigate(R.id.action_navigation_buy_to_listingsFragment)
        }

        binding.otherImageView.setOnClickListener {
            buyViewModel.selectedCategory.value = "Other"
            findNavController().navigate(R.id.action_navigation_buy_to_listingsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
