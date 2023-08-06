package com.dvt.weatherapp.data.repository

import androidx.lifecycle.LiveData
import com.dvt.weatherapp.data.room.daos.CurrentWeatherDao
import com.dvt.weatherapp.data.room.enitities.CurrentWeatherTable

class LocationRepository(private val currentWeatherDao: CurrentWeatherDao) {

    val readAllData: LiveData<List<CurrentWeatherTable>> = currentWeatherDao.readAllData()

    fun addLocation(currentWeatherTable: CurrentWeatherTable){
        currentWeatherDao.addLocation(currentWeatherTable)
    }

    fun updateLocation(currentWeatherTable: CurrentWeatherTable){
        currentWeatherDao.updateLocation(currentWeatherTable)
    }

    fun getCity(id: Int): LiveData<CurrentWeatherTable>{
        return currentWeatherDao.getLocation(id)
    }
}