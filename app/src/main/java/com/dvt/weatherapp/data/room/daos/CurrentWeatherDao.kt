package com.dvt.weatherapp.data.room.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dvt.weatherapp.data.room.enitities.CurrentWeatherTable

@Dao
interface CurrentWeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCurrentWeather(currentWeatherTable: CurrentWeatherTable)

    @Update
    fun updateCurrentWeather(currentWeatherTable: CurrentWeatherTable)

    @Query("SELECT * FROM current_weather_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<CurrentWeatherTable>>

    @Query("SELECT * from current_weather_table WHERE id = :id")
    fun getCurrentWeather(id: Int): LiveData<CurrentWeatherTable>

}