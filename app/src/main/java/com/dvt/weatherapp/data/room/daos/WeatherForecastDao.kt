package com.dvt.weatherapp.data.room.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dvt.weatherapp.data.room.enitities.WeatherForecastTable

@Dao
interface WeatherForecastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addWeatherForecast(weatherForecastTable: WeatherForecastTable)

    @Update
    fun updateLocationForecast(weatherForecastTable: WeatherForecastTable)

    @Query("SELECT * FROM forecast_table ORDER BY id ASC")
    fun readLocationForecast(): LiveData<List<WeatherForecastTable>>

    @Query("SELECT * from forecast_table WHERE id = :id")
    fun getLocationForecast(id: Int): LiveData<WeatherForecastTable>

    @Query("DELETE FROM forecast_table")
    fun clearWeatherForecastTable()

}