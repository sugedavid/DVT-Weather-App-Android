package com.dvt.weatherapp

import android.app.Application
import com.dvt.weatherapp.data.room.db.WeatherDatabase

class DvtWeatherApplication : Application() {
    val database: WeatherDatabase by lazy { WeatherDatabase.getDatabase(this) }

}

