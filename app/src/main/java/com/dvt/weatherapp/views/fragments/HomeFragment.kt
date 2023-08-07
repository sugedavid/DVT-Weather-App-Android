package com.dvt.weatherapp.views.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.dvt.weatherapp.R
import com.dvt.weatherapp.data.utils.Utils
import com.dvt.weatherapp.data.room.enitities.CurrentWeatherTable
import com.dvt.weatherapp.data.view_model.WeatherViewModel
import com.dvt.weatherapp.databinding.FragmentHomeBinding
import com.dvt.weatherapp.data.retrofit.IOpenWeatherMap
import com.dvt.weatherapp.data.retrofit.RetrofitClient
import com.dvt.weatherapp.views.adapter.WeatherForecastAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
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
    private val weatherViewModel: WeatherViewModel by viewModels {
        WeatherViewModel.LocationViewModelFactory(
            activity?.application!!
        )
    }

    // actionbar drawer
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    // places autocomplete code
    private val autocompleteRequestCode = 1
    private var lat = "0.0"
    private var lng = "0.0"

    // location client
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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
        checkLocationPermission(requireActivity(), this.viewLifecycleOwner)

        // grant permission
        binding.btnGrantPermission.setOnClickListener {
            checkLocationPermission(requireActivity(), this.viewLifecycleOwner)
        }

        // permission
        weatherViewModel.permissionAccepted.observe(this.viewLifecycleOwner) { isPermissionAccepted ->
            if (!isPermissionAccepted) binding.btnGrantPermission.visibility = View.VISIBLE
            else binding.btnGrantPermission.visibility = View.INVISIBLE
        }

        // current weather observable
        weatherViewModel.readAllData.observe(this.viewLifecycleOwner) { cities ->

            weatherViewModel.permissionAccepted.observe(this.viewLifecycleOwner) { isPermissionAccepted ->

                if (isPermissionAccepted) {
                    // check if db has current weather info & permissions are accepted
                    if (cities.isEmpty()) {
                        // fetch current weather
                        weatherViewModel.fetchWeatherWorker(lat, lng, false)
                    } else {
                        // display current weather info from db
                        weatherViewModel.getCurrentWeather(Utils().getLocationID(requireContext()))
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
                                    city.isFavourite,
                                    city.latitude,
                                    city.longitude,
                                )
                            }
                    }
                }
            }

        }

        // weather forecast observable
        weatherViewModel.readLocationForecast.observe(this.viewLifecycleOwner) { cityForecast ->

            weatherViewModel.permissionAccepted.observe(this.viewLifecycleOwner) { isPermissionAccepted ->

                if (isPermissionAccepted) {
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
        isFavourite: Boolean,
        cityLat: String,
        cityLng: String,
    ) {
        lat = cityLat
        lng = cityLng

        Utils().saveCondition(requireContext(), main)
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
            getString(R.string.last_refresh, Utils().convertUnixToHour(refreshTime))
        // change background image
        binding.imgBg1.setBackgroundResource(
            Utils().changeBackgroundImage(main)
        )
        // change background color
        binding.constraintLayout.setBackgroundResource(
            Utils().changeBackgroundColor(main)
        )
        binding.navView.constraint_nav_header?.setBackgroundResource(
            Utils().changeBackgroundColor(main)
        )

        // refresh location
        binding.layoutHomeAppbar.imgRefresh.setOnClickListener {
            weatherViewModel.fetchWeatherWorker(cityLat, cityLng, isFavourite)
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
                weatherViewModel.updateCity(
                    CurrentWeatherTable(
                        id = cityID,
                        cityName = cityName,
                        description = description,
                        refreshTime = refreshTime,
                        main = main,
                        temperature = temperature,
                        temperatureMin = temperatureMin,
                        temperatureMax = temperatureMax,
                        isFavourite = !isFavourite,
                        latitude = lat,
                        longitude = lng,
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

    private fun checkLocationPermission(activity: Activity, lifecycleOwner: LifecycleOwner) {
        Dexter.withActivity(activity)
            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        if (ActivityCompat.checkSelfPermission(
                                activity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                activity,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            weatherViewModel.permissionAccepted.value = false
                            return
                        }

                        fusedLocationClient =
                            LocationServices.getFusedLocationProviderClient(activity)
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location: Location? ->
                                // Got last known location. In some rare situations this can be null.
                                weatherViewModel.permissionAccepted.observe(lifecycleOwner) { isPermissionAccepted ->
                                    if (!isPermissionAccepted) {
                                        weatherViewModel.latitude.value =
                                            location?.latitude.toString()
                                        weatherViewModel.longitude.value =
                                            location?.longitude.toString()
                                        weatherViewModel.permissionAccepted.value = true
                                    }
                                }

//                                isPermissionAccepted = true
                                weatherViewModel.permissionAccepted.value = true

                            }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                    Snackbar.make(
                        activity.findViewById(android.R.id.content),
                        activity.getString(R.string.permission_denied),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == autocompleteRequestCode) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)

                        weatherViewModel.latitude.value = place.latLng?.latitude.toString()
                        weatherViewModel.longitude.value = place.latLng?.longitude.toString()

                        // fetch weather info for new location
                        weatherViewModel.fetchWeatherWorker(place.latLng?.latitude.toString(), place.latLng?.longitude.toString(), false)
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