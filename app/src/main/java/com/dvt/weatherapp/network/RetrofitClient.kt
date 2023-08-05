package com.dvt.weatherapp.network

import com.dvt.weatherapp.common.Common
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

object RetrofitClient {
    var instance: Retrofit? = null
        get() {
            if (field == null)
                instance = Retrofit.Builder()
                .baseUrl(Common().baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
            return field
        }
        private set
}