package com.example.pet_universe.ui.accountSettings

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pet_universe.database.ListingDatabase
import com.example.pet_universe.database.RatingRepository
import com.example.pet_universe.databinding.FragmentMyRatingsBinding
import com.example.pet_universe.ui.rating.RatingViewModel
import com.example.pet_universe.ui.rating.RatingViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class MyRatingsFragment : Fragment() {

    private var _binding: FragmentMyRatingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var ratingViewModel: RatingViewModel
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private lateinit var ratingsAdapter: RatingsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyRatingsBinding.inflate(inflater, container, false)

        val database = ListingDatabase.getInstance(requireContext())
        val ratingRepository = RatingRepository(database.ratingDao)
        val factory = RatingViewModelFactory(ratingRepository)
        ratingViewModel = ViewModelProvider(this, factory).get(RatingViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set up RecyclerView
        ratingsAdapter = RatingsAdapter()
        binding.ratingsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ratingsRecyclerView.adapter = ratingsAdapter

        // Observe average rating
        ratingViewModel.averageRating.observe(viewLifecycleOwner) { averageRating ->
            binding.averageRatingTextView.text = "Average Rating: ${String.format("%.1f", averageRating)}"
            binding.averageRatingBar.rating = averageRating
        }

        // Observe list of ratings
        ratingViewModel.getRatingsForUser(currentUserId).observe(viewLifecycleOwner) { ratings ->
            ratingsAdapter.submitList(ratings)
        }

        // Fetch ratings from Firebase
        ratingViewModel.fetchRatingsForUser(currentUserId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
