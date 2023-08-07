package com.dvt.weatherapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dvt.weatherapp.DvtWeatherApplication
import com.dvt.weatherapp.data.repository.WeatherRepository
import com.dvt.weatherapp.data.utils.Utils
import com.dvt.weatherapp.data.room.enitities.CurrentWeatherTable
import com.dvt.weatherapp.data.retrofit.IOpenWeatherMap
import com.dvt.weatherapp.data.retrofit.RetrofitClient
import com.dvt.weatherapp.data.view_model.WeatherViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import retrofit2.Retrofit


class FetchWeatherWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    // rxjava disposable
    private var compositeDisposable: CompositeDisposable? = null

    // retrofit
    private var mService: IOpenWeatherMap? = null

    override suspend fun doWork(): Result {
        val application = applicationContext as DvtWeatherApplication
        val viewModel = WeatherViewModel(application)

        compositeDisposable = CompositeDisposable()
        val retrofit: Retrofit? = RetrofitClient.instance
        mService = retrofit?.create(IOpenWeatherMap::class.java)

        val latitude = inputData.getString("latitude") ?: "0.0"
        val longitude = inputData.getString("longitude") ?: "0.0"
        val isFavourite = inputData.getBoolean("isFavourite", false)

        return try {
            coroutineScope {
                viewModel.getCurrentWeatherInformation(applicationContext, latitude, longitude, isFavourite)
                viewModel.getWeatherForecastInformation(applicationContext, latitude, longitude)
            }
            return Result.success()

        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            Result.failure()
        }

    }
}