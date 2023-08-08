package com.dvt.weatherapp.data.utils

import android.annotation.SuppressLint
import android.content.Context
import com.dvt.weatherapp.R
import java.text.SimpleDateFormat
import java.util.*

class Utils {
    val apiKey = R.string.open_weather_apiKey
    val baseUrl = "https://api.openweathermap.org/data/2.5/"

    // weather states
    enum class Weather {
        Sun, Cloud, Rain, Clear
    }

    // formats date from long format to day of the week and date
    @SuppressLint("SimpleDateFormat")
    fun convertUnixToDate(dt: Long): String? {
        val date = Date(dt * 1000L)
        val simpleDateFormat = SimpleDateFormat("EEE, d MMM, yyy")
        return simpleDateFormat.format(date)
    }

    // formats date from long format to day of the week and time
    @SuppressLint("SimpleDateFormat")
    fun convertUnixToHour(dt: Long): String? {
        val date = Date(dt * 1000L)
        val simpleDateFormat = SimpleDateFormat("EE, h:mm a")
        return simpleDateFormat.format(date)
    }

    // formats date from long format to day of the week
    @SuppressLint("SimpleDateFormat")
    fun convertUnixToDay(dt: Long): String? {
        val date = Date(dt * 1000L)
        val simpleDateFormat = SimpleDateFormat("EEEE")
        return simpleDateFormat.format(date)
    }

    // formats date from long format to time
    fun convertUnixToTime(dt: Long): String? {
        val date = Date(dt * 1000L)
        val simpleDateFormat = SimpleDateFormat("h:mm a")
        return simpleDateFormat.format(date)
    }

    fun changeWeatherImage(condition: String): Int {
        return when (condition) {
            Weather.Rain.toString() -> {
                R.drawable.weather_rain
            }
            Weather.Cloud.toString()  -> {
                R.drawable.weather_partlysunny
            }
            Weather.Sun.toString()  -> {
                R.drawable.weather_clear
            }
            Weather.Clear.toString()  -> {
                R.drawable.weather_clear
            }
            else -> {
                R.drawable.weather_partlysunny
            }
        }
    }

    fun changeBackgroundImage(condition: String): Int {
        return when (condition) {
            Weather.Rain.toString() -> {
                R.drawable.forest_rainy
            }
            Weather.Cloud.toString()  -> {
                R.drawable.forest_cloudy
            }
            Weather.Sun.toString()  -> {
                R.drawable.forest_sunny
            }
            Weather.Clear.toString()  -> {
                R.drawable.forest_sunny
            }
            else -> {
                R.drawable.forest_cloudy
            }
        }
    }

    fun changeBackgroundColor(condition: String): Int {
        return when (condition){
            Weather.Rain.toString()  -> {
                R.color.color_rainy
            }
            Weather.Cloud.toString()  -> {
                R.color.color_cloudy
            }
            Weather.Sun.toString()  -> {
                R.color.color_sunny
            }
            Weather.Clear.toString()  -> {
                R.color.color_sunny
            }
            else -> {
                R.color.color_cloudy
            }
        }
    }

    fun changeFavouriteImage(isFavourite: Boolean): Int {
        return if (isFavourite) {
            R.drawable.ic_heart_white
        } else {
            R.drawable.ic_heart_outline
        }
    }

    fun saveLocationID(context: Context, locationID: Int) {
        val sharedPreference = context.getSharedPreferences("CITY_ID", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putInt("cityID", locationID)
        editor.apply()
    }

    fun getLocationID(context: Context): Int {
        val sharedPreference = context.getSharedPreferences("CITY_ID", Context.MODE_PRIVATE)
        return sharedPreference.getInt("cityID", 3163858)
    }

    fun saveCondition(context: Context, condition: String) {
        val sharedPreference = context.getSharedPreferences("CONDITION", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("condition", condition)
        editor.apply()
    }

    fun getCondition(context: Context): String {
        val sharedPreference = context.getSharedPreferences("CONDITION", Context.MODE_PRIVATE)
        return sharedPreference.getString("condition", "sun").toString()
    }
}