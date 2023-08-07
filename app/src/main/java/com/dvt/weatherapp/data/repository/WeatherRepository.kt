package com.dvt.weatherapp.data.repository

import androidx.lifecycle.LiveData
import com.dvt.weatherapp.data.room.daos.CurrentWeatherDao
import com.dvt.weatherapp.data.room.daos.WeatherForecastDao
import com.dvt.weatherapp.data.room.enitities.CurrentWeatherTable

class WeatherRepository(private val currentWeatherDao: CurrentWeatherDao, private val weatherForecastDao: WeatherForecastDao) {

    val readAllData: LiveData<List<CurrentWeatherTable>> = currentWeatherDao.readAllData()

    fun addCurrentWeather(currentWeatherTable: CurrentWeatherTable){
        currentWeatherDao.addCurrentWeather(currentWeatherTable)
    }

    fun updateCurrentWeather(currentWeatherTable: CurrentWeatherTable){
        currentWeatherDao.updateCurrentWeather(currentWeatherTable)
    }

    fun getCity(id: Int): LiveData<CurrentWeatherTable>{
        return currentWeatherDao.getCurrentWeather(id)
    }

    fun nukeWeatherForecast() {
        return weatherForecastDao.nukeWeatherForecastTable()
    }
}