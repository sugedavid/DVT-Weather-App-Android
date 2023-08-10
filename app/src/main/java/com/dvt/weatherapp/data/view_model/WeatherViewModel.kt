package com.dvt.weatherapp.data.view_model

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.*
import androidx.work.*
import com.dvt.weatherapp.data.room.db.WeatherDatabase
import com.dvt.weatherapp.data.utils.Utils
import com.dvt.weatherapp.data.room.enitities.WeatherForecastTable
import com.dvt.weatherapp.data.room.enitities.CurrentWeatherTable
import com.dvt.weatherapp.data.retrofit.IOpenWeatherMap
import com.dvt.weatherapp.data.retrofit.RetrofitClient
import com.dvt.weatherapp.data.repository.WeatherRepository
import com.dvt.weatherapp.worker.FetchWeatherWorker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class WeatherViewModel(application: Application) : ViewModel() {

    val readAllData: LiveData<List<CurrentWeatherTable>>
    private val repository: WeatherRepository
    private val _locationDao = WeatherDatabase.getDatabase(application).currentWeatherDao()
    private val _forecastDao = WeatherDatabase.getDatabase(application).weatherForecastDao()
    private var _compositeDisposable: CompositeDisposable? = null
    private var _mService: IOpenWeatherMap? = null
    private val _retrofit: Retrofit? = RetrofitClient.instance

    // workManager
    private val _workManager = WorkManager.getInstance(application.applicationContext)

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

    var permissionAccepted = MutableLiveData(false)


    // weather forecast
    private val _readLocationForecast: LiveData<List<WeatherForecastTable>>
    var readLocationForecast: LiveData<List<WeatherForecastTable>>
    private val _locationForecastDao =
        WeatherDatabase.getDatabase(application).weatherForecastDao()

    init {
        _compositeDisposable = CompositeDisposable()
        _mService = _retrofit?.create(IOpenWeatherMap::class.java)
        repository = WeatherRepository(_locationDao, _forecastDao)
        readAllData = repository.readAllData
        _readLocationForecast = _locationForecastDao.readLocationForecast()
        readLocationForecast = _readLocationForecast
    }

    private fun addCurrentWeather(currentWeatherTable: CurrentWeatherTable) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addCurrentWeather(currentWeatherTable)
        }
    }

    fun updateCity(currentWeatherTable: CurrentWeatherTable) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCurrentWeather(currentWeatherTable)
        }
    }

    private fun clearWeatherForecastDb() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearWeatherForecast()
        }
    }

    fun getCurrentWeather(id: Int): LiveData<CurrentWeatherTable> {
        return _locationDao.getCurrentWeather(id)
    }

    private fun addWeatherForecast(weatherForecastTable: WeatherForecastTable) {
        viewModelScope.launch(Dispatchers.IO) {
            _locationForecastDao.addWeatherForecast(weatherForecastTable)
        }
    }

    // fetches current weather info
    fun getCurrentWeatherInformation(context: Context, lat: String, lng: String, isFavourite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _compositeDisposable?.add(
                _mService!!.getWeatherByLatLng(
                    lat, lng,
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
                        addCurrentWeather(
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
                                latitude = lat,
                                longitude = lng,
                            )
                        )
                        // save locationID & condition to preference
                        Utils().saveLocationID(context, cityId)
                        Utils().saveCondition(context, description)

                    }, { throwable ->
                        Toast.makeText(
                            context,
                            throwable.message,
                            Toast.LENGTH_SHORT
                        ).show()

                    })
            )
        }
    }

    // fetches daily forecast info
    fun getWeatherForecastInformation(context: Context, lat: String, lng: String,) {

        viewModelScope.launch {
            // clear forecast data from db
            clearWeatherForecastDb()

            _compositeDisposable!!.add(
                _mService!!.getForecastWeatherByLatLng(
                    lat, lng,
                    context.getString(Utils().apiKey),
                    "metric"
                )
                !!.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ weatherForecastResult ->
                        val forecastId = weatherForecastResult?.city?.id ?: 0
                        for (result in weatherForecastResult?.list!!) {
                            val main = result.weather?.get(0)?.main ?: ""
                            val forecastDay = result.dt
                            val forecastTemperature = result.main?.temp?.toInt() ?: 0

                            // save weatherForecastResult to db
                            addWeatherForecast(
                                WeatherForecastTable(
                                    id = 0, day = forecastDay, main = main,
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

    // fetches weather data every 1 hour in the background using work manager
    fun fetchWeatherWorker(lat: String, lng: String, isFavourite: Boolean) {

        // internet connection constraint
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // input data
        val data = Data.Builder()
            .putString("latitude", lat)
            .putString("longitude", lng)
            .putBoolean("isFavourite", isFavourite)
            .build()

        // workRequest to fetch weather data every 1 hour in the background
        val workRequest = PeriodicWorkRequest.Builder(
            FetchWeatherWorker::class.java,
            1,
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInputData(data)
            .build()
        _workManager.enqueue(workRequest)
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

