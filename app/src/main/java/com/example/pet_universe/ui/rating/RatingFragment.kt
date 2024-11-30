package com.example.pet_universe.ui.rating

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.pet_universe.database.ListingDatabase
import com.example.pet_universe.database.RatingRepository
import com.example.pet_universe.databinding.FragmentRatingBinding
import com.example.pet_universe.ui.rating.RatingViewModel
import com.example.pet_universe.ui.rating.RatingViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class RatingFragment : Fragment() {

    private var _binding: FragmentRatingBinding? = null
    private val binding get() = _binding!!

    private lateinit var ratingViewModel: RatingViewModel

    private val args: RatingFragmentArgs by navArgs()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private lateinit var toUserId: String // The user being rated

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRatingBinding.inflate(inflater, container, false)

        val database = ListingDatabase.getInstance(requireContext())
        val ratingRepository = RatingRepository(database.ratingDao)
        val factory = RatingViewModelFactory(ratingRepository)
        ratingViewModel = ViewModelProvider(this, factory).get(RatingViewModel::class.java)

        toUserId = args.toUserId

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Fetch and display average rating
        ratingViewModel.averageRating.observe(viewLifecycleOwner) { averageRating ->
            binding.averageRatingTextView.text = "Average Rating: ${String.format("%.1f", averageRating)}"
            binding.averageRatingBar.rating = averageRating
        }
        ratingViewModel.fetchAverageRating(toUserId)

        // Handle rating submission
        binding.submitRatingButton.setOnClickListener {
            val ratingValue = binding.submitRatingBar.rating.toInt()
            if (ratingValue > 0) {
                val rating = com.example.pet_universe.database.Rating(
                    fromUserId = currentUserId,
                    toUserId = toUserId,
                    ratingValue = ratingValue
                )
                ratingViewModel.submitRating(rating)
                Toast.makeText(requireContext(), "Rating submitted", Toast.LENGTH_SHORT).show()
                // Optionally, navigate back or close the fragment
                requireActivity().onBackPressed()
            } else {
                Toast.makeText(requireContext(), "Please select a rating", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
