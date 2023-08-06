package com.dvt.weatherapp.data.retrofit

import com.dvt.weatherapp.data.utils.Utils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

object RetrofitClient {
    var instance: Retrofit? = null
        get() {
            if (field == null)
                instance = Retrofit.Builder()
                .baseUrl(Utils().baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
            return field
        }
        private set
}