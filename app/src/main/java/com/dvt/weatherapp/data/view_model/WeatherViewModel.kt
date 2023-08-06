package com.dvt.weatherapp.data.view_model

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.CountDownTimer
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import androidx.work.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.dvt.weatherapp.R
import com.dvt.weatherapp.data.room.db.WeatherDatabase
import com.dvt.weatherapp.data.utils.Utils
import com.dvt.weatherapp.data.room.enitities.WeatherForecastTable
import com.dvt.weatherapp.data.room.enitities.CurrentWeatherTable
import com.dvt.weatherapp.data.retrofit.IOpenWeatherMap
import com.dvt.weatherapp.data.retrofit.RetrofitClient
import com.dvt.weatherapp.data.repository.LocationRepository
import com.dvt.weatherapp.worker.FetchWeatherWorker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class WeatherViewModel(application: Application) : ViewModel() {

    val readAllData: LiveData<List<CurrentWeatherTable>>
    private val repository: LocationRepository
    private val _locationDao = WeatherDatabase.getDatabase(application).locationDao()
    private var _compositeDisposable: CompositeDisposable? = null
    private var _mService: IOpenWeatherMap? = null
    private val _retrofit: Retrofit? = RetrofitClient.instance

    // workManager
    private val _workManager = WorkManager.getInstance(application.applicationContext)

    // location client
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // current weather info
    private var cityId = 0
    private var cityName = ""
    private var description = ""
    private var main = ""
    private var refreshTime: Long = 0
    private var temperature: Int = 0
    private var temperatureMax: Int = 0
    private var temperatureMin: Int = 0

    // current location lat & lng
    var latitude = MutableLiveData("0.0")
    var longitude = MutableLiveData("0.0")

    // loading
    private var _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // permission
    private var _isPermissionAccepted = MutableLiveData(false)
    val isPermissionAccepted: LiveData<Boolean> = _isPermissionAccepted

    // weather forecast
    private val _readLocationForecast: LiveData<List<WeatherForecastTable>>
    var readLocationForecast: LiveData<List<WeatherForecastTable>>
    private val _locationForecastDao =
        WeatherDatabase.getDatabase(application).locationForecastDao()

    init {
        _compositeDisposable = CompositeDisposable()
        _mService = _retrofit?.create(IOpenWeatherMap::class.java)
        repository = LocationRepository(_locationDao)
        readAllData = repository.readAllData
        _readLocationForecast = _locationForecastDao.readLocationForecast()
        readLocationForecast = _readLocationForecast
    }

    fun addLocation(currentWeatherTable: CurrentWeatherTable) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addLocation(currentWeatherTable)
        }
    }

    fun updateCity(currentWeatherTable: CurrentWeatherTable) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateLocation(currentWeatherTable)
        }
    }

    fun getLocation(id: Int): LiveData<CurrentWeatherTable> {
        return _locationDao.getLocation(id)
    }

    fun addLocationForecast(weatherForecastTable: WeatherForecastTable) {
        viewModelScope.launch(Dispatchers.IO) {
            _locationForecastDao.addLocationForecast(weatherForecastTable)
        }
    }

    // fetch current weather info
    fun getCurrentWeatherInformation(context: Context, isFavourite: Boolean) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            _compositeDisposable?.add(
                _mService!!.getWeatherByLatLng(
                    latitude.value, longitude.value,
                    context.getString(Utils().apiKey),
                    "metric"
                )
                !!.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ weatherResult ->
                        cityId = weatherResult?.id ?: 0
                        cityName = weatherResult?.name ?: ""
                        description = weatherResult?.weather?.get(0)?.description ?: ""
                        main = weatherResult?.weather?.get(0)?.main ?: ""
                        refreshTime = weatherResult?.dt!!.toLong()
                        temperature = weatherResult.main?.temp?.toInt() ?: 0
                        temperatureMin = weatherResult.main?.temp_min?.toInt() ?: 0
                        temperatureMax = weatherResult.main?.temp_max?.toInt() ?: 0

                        // save weather info to db
                        addLocation(
                            CurrentWeatherTable(
                                id = cityId,
                                cityName = cityName,
                                description = description,
                                main = main,
                                refreshTime = refreshTime,
                                temperature = temperature,
                                temperatureMin = temperatureMin,
                                temperatureMax = temperatureMax,
                                isFavourite = isFavourite,
                                latitude = latitude.value ?: "0.0",
                                longitude = longitude.value ?: "0.0",
                            )
                        )
                        // save locationID & condition to preference
                        Utils().saveLocationID(context, cityId)
                        Utils().saveCondition(context, description)

                        // start worker
                        fetchWeatherCounter()

                        _isLoading.value = false
                    }, { throwable ->
                        _isLoading.value = false
                        Toast.makeText(
                            context,
                            throwable.message,
                            Toast.LENGTH_SHORT
                        ).show()

                    })
            )
        }
    }

    // fetch forecast info
    fun getForecastWeatherInformation(context: Context) {

        viewModelScope.launch {
            _compositeDisposable!!.add(
                _mService!!.getForecastWeatherByLatLng(
                    latitude.value, longitude.value,
                    context.getString(Utils().apiKey),
                    "metric"
                )
                !!.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ weatherForecastResult ->


                        for (result in weatherForecastResult?.list!!) {
                            val forecastId = result.weather?.get(0)?.id ?: 0
                            val main = result.weather?.get(0)?.main ?: ""
                            val forecastDay = result.dt
                            val forecastTemperature = result.main?.temp?.toInt() ?: 0

                            // save weatherForecastResult to db
                            addLocationForecast(
                                WeatherForecastTable(
                                    id = forecastId, day = forecastDay, main = main,
                                    temperature = forecastTemperature
                                )
                            )
                        }
                    }
                    ) { throwable ->
                        Toast.makeText(
                            context,
                            throwable.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            )
        }

    }

    private fun fetchWeatherWorker() {

        val builder = Data.Builder()
        builder.putString("latitude", latitude.value)
        builder.putString("longitude", latitude.value)

        // internet connection constraint
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val fetchLocationWorkRequest: WorkRequest =
            OneTimeWorkRequestBuilder<FetchWeatherWorker>()
                .setInputData(builder.build())
                .setConstraints(constraints)
                .build()
        _workManager.enqueue(fetchLocationWorkRequest)

    }

    // fetch weather every hour
    fun fetchWeatherCounter() {
        val timer = object : CountDownTimer(900000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                fetchWeatherWorker()
            }
        }
        timer.start()
    }

    // checks for location permissions
    fun checkLocationPermission(activity: Activity) {
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
                            _isPermissionAccepted.value = false
                            return
                        }

                        fusedLocationClient =
                            LocationServices.getFusedLocationProviderClient(activity)
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location: Location? ->
                                // Got last known location. In some rare situations this can be null.
                                if(!isPermissionAccepted.value!!){
                                    latitude.value = location?.latitude.toString()
                                    longitude.value = location?.longitude.toString()
                                }

                                _isPermissionAccepted.value = true

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

    class LocationViewModelFactory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WeatherViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct view_model")
        }
    }
}

