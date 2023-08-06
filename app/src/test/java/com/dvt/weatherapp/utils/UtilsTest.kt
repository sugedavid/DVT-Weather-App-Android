package com.dvt.weatherapp.utils

import com.dvt.weatherapp.R
import com.dvt.weatherapp.data.utils.Utils
import junit.framework.Assert.assertEquals
import org.junit.Test

class UtilsTest {
    private val longDate: Long = 1212580300

    @Test
    fun convertUnixToDateIsCorrect() {
        assertEquals(Utils().convertUnixToDate(longDate), "Wed, 4 Jun, 2008")
    }

    @Test
    fun convertUnixToDayIsCorrect() {
        assertEquals(Utils().convertUnixToDay(longDate), "Wednesday")
    }

    @Test
    fun convertUnixToHourIsCorrect() {
        assertEquals(Utils().convertUnixToHour(longDate), "Wed, 2:51 pm")
    }

    @Test
    fun changeFavouriteImageIsCorrect() {
        assertEquals(Utils().changeFavouriteImage(true), R.drawable.ic_heart_white)
        assertEquals(Utils().changeFavouriteImage(false), R.drawable.ic_heart_outline)
    }

    @Test
    fun changeWeatherImageIsCorrect() {
        assertEquals(Utils().changeWeatherImage("Rain"), R.drawable.weather_rain)
        assertEquals(Utils().changeWeatherImage("Sun"), R.drawable.weather_clear)
        assertEquals(Utils().changeWeatherImage("Cloud"), R.drawable.weather_partlysunny)
    }

    @Test
    fun changeBackgroundImageIsCorrect() {
        assertEquals(Utils().changeBackgroundImage("Rain"), R.drawable.forest_rainy)
        assertEquals(Utils().changeBackgroundImage("Sun"), R.drawable.forest_sunny)
        assertEquals(Utils().changeBackgroundImage("Cloud"), R.drawable.forest_cloudy)
    }

    @Test
    fun changeBackgroundColorIsCorrect() {
        assertEquals(Utils().changeBackgroundColor("Rain"), R.color.color_rainy)
        assertEquals(Utils().changeBackgroundColor("Sun"), R.color.color_sunny)
        assertEquals(Utils().changeBackgroundColor("Cloud"), R.color.color_cloudy)
    }
}