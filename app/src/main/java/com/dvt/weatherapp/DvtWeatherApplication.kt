package com.dvt.weatherapp

import android.app.Application
import com.dvt.weatherapp.data.room.db.WeatherDatabase

/**
 * Override application to setup background work via WorkManager
 */
class DvtWeatherApplication : Application() {
    val database: WeatherDatabase by lazy { WeatherDatabase.getDatabase(this) }

}

