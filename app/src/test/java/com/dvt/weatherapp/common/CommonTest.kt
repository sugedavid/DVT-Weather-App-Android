package com.dvt.weatherapp.common

import com.dvt.weatherapp.R
import junit.framework.Assert.assertEquals
import org.junit.Test

class CommonTest {
    private val longDate: Long = 1212580300

    @Test
    fun convertUnixToDateIsCorrect() {
        assertEquals(Common().convertUnixToDate(longDate), "Wed, 4 Jun, 2008")
    }

    @Test
    fun convertUnixToDayIsCorrect() {
        assertEquals(Common().convertUnixToDay(longDate), "Wednesday")
    }

    @Test
    fun convertUnixToHourIsCorrect() {
        assertEquals(Common().convertUnixToHour(longDate), "Wed, 2:51 PM")
    }

    @Test
    fun changeFavouriteImageIsCorrect() {
        assertEquals(Common().changeFavouriteImage(true), R.drawable.ic_heart_white)
        assertEquals(Common().changeFavouriteImage(false), R.drawable.ic_heart_outline)
    }

    @Test
    fun changeWeatherImageIsCorrect() {
        assertEquals(Common().changeWeatherImage("Rain"), R.drawable.weather_rain)
        assertEquals(Common().changeWeatherImage("Sun"), R.drawable.weather_clear)
        assertEquals(Common().changeWeatherImage("Cloud"), R.drawable.weather_partlysunny)
    }

    @Test
    fun changeBackgroundImageIsCorrect() {
        assertEquals(Common().changeBackgroundImage("Rain"), R.drawable.forest_rainy)
        assertEquals(Common().changeBackgroundImage("Sun"), R.drawable.forest_sunny)
        assertEquals(Common().changeBackgroundImage("Cloud"), R.drawable.forest_cloudy)
    }

    @Test
    fun changeBackgroundColorIsCorrect() {
        assertEquals(Common().changeBackgroundColor("Rain"), R.color.color_rainy)
        assertEquals(Common().changeBackgroundColor("Sun"), R.color.color_sunny)
        assertEquals(Common().changeBackgroundColor("Cloud"), R.color.color_cloudy)
    }
}