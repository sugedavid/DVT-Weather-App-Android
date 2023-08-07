package com.dvt.weatherapp.data.room.enitities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forecast_table")
data class WeatherForecastTable (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val day: Long,
    val main: String,
    val temperature: Int,
    )