package com.dvt.weatherapp.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.dvt.weatherapp.R
import com.dvt.weatherapp.data.view_model.WeatherViewModel
import com.dvt.weatherapp.databinding.FragmentMapsBinding


class MapsFragment : Fragment(), OnMapReadyCallback {

    // view model
    private val weatherViewModel: WeatherViewModel by viewModels{
        WeatherViewModel.LocationViewModelFactory(
            activity?.application!!
        )
    }

    // view binding
    private var _binding: FragmentMapsBinding? = null
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
        _binding = FragmentMapsBinding.inflate(inflater, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //back button
        binding.imgBackBtn.setOnClickListener {
            val action = MapsFragmentDirections.actionMapFragmentToFavouritesFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {

        // location view model
        weatherViewModel.readAllData.observe(this.viewLifecycleOwner) { location ->

            // check if db has forecast weather info
            if ((location.isNotEmpty() && !location.none { it.isFavourite })) {

                // loop through data & add markers
                for (item in location) {
                    Log.d("map_data", item.cityName)
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(item.latitude.toDouble(), item.longitude.toDouble()))
                            .title("${item.cityName}")
                    )

                }

                // animate to first favourite item
                val coordinate = LatLng(
                    location[0].latitude.toDouble(),
                    location[0].longitude.toDouble()
                )

                val locationAnimate = CameraUpdateFactory.newLatLngZoom(
                    coordinate, 3f
                )
                googleMap.moveCamera(locationAnimate)

            }
        }
    }

}