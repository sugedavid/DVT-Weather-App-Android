package com.dvt.weatherapp

import android.app.Application
import com.dvt.weatherapp.data.location.LocationDatabase

/**
 * Override application to setup background work via WorkManager
 */
class DvtWeatherApplication : Application() {
    val database: LocationDatabase by lazy { LocationDatabase.getDatabase(this) }

}

