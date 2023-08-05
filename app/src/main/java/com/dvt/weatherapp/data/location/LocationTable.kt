package com.dvt.weatherapp.data.location

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_table")
data class LocationTable (
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val cityName: String,
    val description: String,
    val main: String,
    val refreshTime: Long,
    val temperature: Int,
    val temperatureMin: Int,
    val temperatureMax: Int,
    var isFavourite: Boolean,
    val latitude: String,
    val longitude: String,
    )