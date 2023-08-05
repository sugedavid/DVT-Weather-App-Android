package com.dvt.weatherapp.presentation.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.dvt.weatherapp.R
import com.dvt.weatherapp.common.Common
import com.dvt.weatherapp.data.location.LocationTable
import com.dvt.weatherapp.data.location.LocationViewModel
import com.dvt.weatherapp.databinding.FragmentHomeBinding
import com.dvt.weatherapp.network.IOpenWeatherMap
import com.dvt.weatherapp.network.RetrofitClient
import com.dvt.weatherapp.presentation.adapter.WeatherForecastAdapter
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.nav_header.view.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import java.util.*


class HomeFragment : Fragment() {

    // rxjava disposable
    private var compositeDisposable: CompositeDisposable? = null

    // retrofit
    private var mService: IOpenWeatherMap? = null

    // binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // view models
    private val locationViewModel: LocationViewModel by viewModels {
        LocationViewModel.LocationViewModelFactory(
            activity?.application!!
        )
    }

    // actionbar drawer
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    // places autocomplete code
    private val autocompleteRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compositeDisposable = CompositeDisposable()
        val retrofit: Retrofit? = RetrofitClient.instance
        mService = retrofit?.create(IOpenWeatherMap::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // drawer setup
        actionBarDrawerToggle = ActionBarDrawerToggle(
            requireActivity(),
            binding.drawerLayout,
            R.string.nav_open,
            R.string.nav_close
        )
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        binding.layoutHomeAppbar.imgDrawerMenu.setOnClickListener {
            if (binding.drawerLayout.isDrawerVisible(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_favourites -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToFavouritesFragment()
                    findNavController().navigate(action)
                }
            }
            true
        }

        // Google Places
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.places_apiKey), Locale.US);
        }
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        // search menu button
        binding.layoutHomeAppbar.imgSearchTab.setOnClickListener {
            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(requireContext())
            startActivityForResult(intent, autocompleteRequestCode)
        }

        // check location permissions
        locationViewModel.checkLocationPermission(requireActivity())

        // loading state
        locationViewModel.isLoading.observe(this.viewLifecycleOwner) {
            if (it) {
                binding.loadingF.visibility = View.VISIBLE
                binding.view.visibility = View.INVISIBLE
            } else {
                binding.loadingF.visibility = View.GONE
                binding.view.visibility = View.VISIBLE
            }
        }

        // current weather observable
        locationViewModel.readAllData.observe(this.viewLifecycleOwner) { cities ->

            locationViewModel.isPermissionAccepted.observe(this.viewLifecycleOwner) { isPermissionAccepted ->
                // check if db has current weather info & permissions are accepted
                if (cities.isEmpty() && isPermissionAccepted) {
                    // fetch current weather
                    locationViewModel.getCurrentWeatherInformation(requireContext())
                } else if (cities.isNotEmpty() && isPermissionAccepted) {
                    // display current weather info from db
                    locationViewModel.getLocation(Common().getLocationID(requireContext()))
                        .observe(this.viewLifecycleOwner) { city ->
                            updateViews(
                                city.id,
                                city.cityName,
                                city.description,
                                city.main,
                                city.temperature,
                                city.temperatureMin,
                                city.temperatureMax,
                                city.refreshTime,
                                city.isFavourite
                            )
                        }
                }
            }

        }

        // weather forecast observable
        locationViewModel.readLocationForecast.observe(this.viewLifecycleOwner) { cityForecast ->

            locationViewModel.isPermissionAccepted.observe(this.viewLifecycleOwner) { isPermissionAccepted ->

                // check if db has forecast weather info & permissions are accepted
                if (cityForecast.isEmpty() && isPermissionAccepted) {
                    //  fetch 5 day forecast
                    locationViewModel.getForecastWeatherInformation(requireContext())
                } else if (cityForecast.isNotEmpty() && isPermissionAccepted) {
                    // load forecast weather info to recyclerview
                    val adapter = WeatherForecastAdapter(requireContext(), cityForecast)
                    val recyclerView = binding.recyclerForecast
                    recyclerView.layoutManager = LinearLayoutManager(requireContext())
                    recyclerView.adapter = adapter
                }
            }
        }

        return view
    }

    // updates the ui with data from db
    private fun updateViews(
        cityID: Int,
        cityName: String,
        description: String,
        main: String,
        temperature: Int,
        temperatureMin: Int,
        temperatureMax: Int,
        refreshTime: Long,
        isFavourite: Boolean
    ) {

        Common().saveCondition(requireContext(), main)
        // city name
        binding.txtCurrentLocation.text = cityName
        // weather description
        binding.txtWeatherDesc.text = description
        // current temperature
        binding.txtTemp.text = getString(R.string.temp, temperature)
        binding.txtTempCurrent.text = getString(R.string.temp_current, temperature)
        // min temperature
        binding.txtTempMin.text = getString(R.string.temp_min, temperatureMin)
        // max temperature
        binding.txtTempMax.text = getString(R.string.temp_max, temperatureMax)
        //date
        binding.txtDateTime.text =
            getString(R.string.last_refresh, Common().convertUnixToHour(refreshTime))
        // change background image
        binding.imgBg1.setBackgroundResource(
            Common().changeBackgroundImage(main)
        )
        // change background color
        binding.constraintLayout.setBackgroundResource(
            Common().changeBackgroundColor(main)
        )
        binding.navView.constraint_nav_header?.setBackgroundResource(
            Common().changeBackgroundColor(main)
        )

        // refresh location
        binding.layoutHomeAppbar.imgRefresh.setOnClickListener {
            locationViewModel.getCurrentWeatherInformation(requireContext())
            locationViewModel.getForecastWeatherInformation(requireContext())
        }

        // favourite location
        binding.layoutHomeAppbar.imgWeatherFav.setImageResource(
            when {
                isFavourite -> {
                    R.drawable.ic_heart_white
                }
                else -> {
                    R.drawable.ic_heart_outline
                }
            }
        )
        binding.layoutHomeAppbar.imgWeatherFav.setOnClickListener {
            lifecycleScope.launch {
                // save weatherForecastResult to db
                locationViewModel.updateCity(
                    LocationTable(
                        id = cityID,
                        cityName = cityName,
                        description = description,
                        refreshTime = refreshTime,
                        main = main,
                        temperature = temperature,
                        temperatureMin = temperatureMin,
                        temperatureMax = temperatureMax,
                        isFavourite = !isFavourite,
                        latitude = locationViewModel.latitude.value!!,
                        longitude = locationViewModel.longitude.value!!,
                    )
                )

                if (!isFavourite) Toast.makeText(
                    requireContext(),
                    "$cityName added to your favourites",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == autocompleteRequestCode) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)

                        locationViewModel.latitude.value = place.latLng?.latitude.toString()
                        locationViewModel.longitude.value = place.latLng?.longitude.toString()
                        // fetch weather info for new location
                        locationViewModel.getCurrentWeatherInformation(requireContext())
                        locationViewModel.getForecastWeatherInformation(requireContext())
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Toast.makeText(
                            requireContext(),
                            "An error occurred: ${status.statusMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}