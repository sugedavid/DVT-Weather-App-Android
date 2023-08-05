package com.dvt.weatherapp.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dvt.weatherapp.common.Common
import com.dvt.weatherapp.data.location.LocationViewModel
import com.dvt.weatherapp.databinding.FragmentFavouritesBinding
import com.dvt.weatherapp.presentation.adapter.FavouritesAdapter

class FavouritesFragment : Fragment() {

    // view model
    private val locationViewModel: LocationViewModel by viewModels{
        LocationViewModel.LocationViewModelFactory(
            activity?.application!!
        )
    }

    // view binding
    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        val view = binding.root

        // location view model
        locationViewModel.readAllData.observe(this.viewLifecycleOwner) { city ->

            // check if db has forecast weather info
            if ((city.isEmpty() || city.none { it.isFavourite })) {
                //  empty state
                binding.txtNoFavourites.visibility = View.VISIBLE
            } else {
                binding.txtNoFavourites.visibility = View.GONE
                // load forecast weather info from db to recyclerview
                val adapter = FavouritesAdapter(requireContext(), city, locationViewModel)
                val recyclerView = binding.recyclerForecast
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = adapter
            }
        }

        //back button
        binding.imgBackBtn.setOnClickListener {
            val action = FavouritesFragmentDirections.actionFavouritesFragmentToHomeFragment()
            findNavController().navigate(action)
        }
        //map button
        binding.imgMapMenu.setOnClickListener {
            val action = FavouritesFragmentDirections.actionFavouritesFragmentToMapFragment()
            findNavController().navigate(action)
        }
        // change background color
        binding.constraintLayout.setBackgroundResource(
            Common().changeBackgroundColor(Common().getCondition(requireContext()))
        )

        return view
    }
}