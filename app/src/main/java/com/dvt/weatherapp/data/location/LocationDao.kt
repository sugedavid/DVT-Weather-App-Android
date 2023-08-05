package com.dvt.weatherapp.data.location

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLocation(locationTable: LocationTable)

    @Update
    fun updateLocation(locationTable: LocationTable)

    @Query("SELECT * FROM location_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<LocationTable>>

    @Query("SELECT * from location_table WHERE id = :id")
    fun getLocation(id: Int): LiveData<LocationTable>

}