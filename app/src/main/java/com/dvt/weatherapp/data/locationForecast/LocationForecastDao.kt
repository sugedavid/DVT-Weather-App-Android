package com.dvt.weatherapp.data.locationForecast

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LocationForecastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLocationForecast(locationForecastTable: LocationForecastTable)

    @Update
    fun updateLocationForecast(locationForecastTable: LocationForecastTable)

    @Query("SELECT * FROM city_forecast_table ORDER BY id ASC")
    fun readLocationForecast(): LiveData<List<LocationForecastTable>>

    @Query("SELECT * from city_forecast_table WHERE id = :id")
    fun getLocationForecast(id: Int): LiveData<LocationForecastTable>

}