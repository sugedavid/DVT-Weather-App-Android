package com.dvt.weatherapp.views.fragments

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.dvt.weatherapp.R
import com.dvt.weatherapp.data.room.daos.CurrentWeatherDao
import com.dvt.weatherapp.data.room.db.WeatherDatabase
import com.dvt.weatherapp.data.room.enitities.CurrentWeatherTable
import com.dvt.weatherapp.views.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException

class MapsFragmentTest {

    private lateinit var currentWeatherDao: CurrentWeatherDao
    private lateinit var database: WeatherDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, WeatherDatabase::class.java
        ).build()
        currentWeatherDao = database.currentWeatherDao()

        // fill data to db
        val currentWeatherTable = CurrentWeatherTable(
            id = 0,
            cityName = "Nairobi",
            description = "description",
            main = "Rain",
            refreshTime = 1665348664414,
            temperature = 12,
            temperatureMin = 11,
            temperatureMax = 13,
            isFavourite = true,
            latitude = "0.0",
            longitude = "0.0",
        )

        currentWeatherDao.addCurrentWeather(currentWeatherTable)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun launchFragmentAndVerifyUI() {
        // use launchInContainer to launch the fragment with UI
        launchFragmentInContainer<MapsFragment>()
    }

    @Test
    fun shouldNavigateToFavouriteFragment() {
        launchActivity<MainActivity>()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({

            // navigate to map fragment
            val backView = ViewMatchers.withId(R.id.imgBackBtn)
            onView(backView)
                .check(ViewAssertions.matches(isDisplayed()))
            onView(backView)
                .perform(ViewActions.click())

            // assert
            val mapView = ViewMatchers.withId(R.id.imgMapMenu)
            onView(mapView)
                .check(ViewAssertions.matches(isDisplayed()))

        }, 5000)
    }

}