package com.dvt.weatherapp.data.room.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dvt.weatherapp.data.room.daos.WeatherForecastDao
import com.dvt.weatherapp.data.room.enitities.WeatherForecastTable
import com.dvt.weatherapp.data.room.daos.CurrentWeatherDao
import com.dvt.weatherapp.data.room.enitities.CurrentWeatherTable

@Database(
    entities = [CurrentWeatherTable::class, WeatherForecastTable::class],
    version = 1,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun locationDao(): CurrentWeatherDao
    abstract fun locationForecastDao(): WeatherForecastDao

    companion object {
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getDatabase(context: Context): WeatherDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "weather_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}