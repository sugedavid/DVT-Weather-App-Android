package com.dvt.weatherapp.data.room.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dvt.weatherapp.data.room.enitities.CurrentWeatherTable

@Dao
interface CurrentWeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLocation(currentWeatherTable: CurrentWeatherTable)

    @Update
    fun updateLocation(currentWeatherTable: CurrentWeatherTable)

    @Query("SELECT * FROM location_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<CurrentWeatherTable>>

    @Query("SELECT * from location_table WHERE id = :id")
    fun getLocation(id: Int): LiveData<CurrentWeatherTable>

}