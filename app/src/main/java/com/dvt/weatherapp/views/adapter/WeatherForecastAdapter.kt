package com.dvt.weatherapp.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.dvt.weatherapp.R
import com.dvt.weatherapp.data.utils.Utils
import com.dvt.weatherapp.data.room.enitities.WeatherForecastTable


class WeatherForecastAdapter(
    var context: Context,
    private var weatherForecastResult: List<WeatherForecastTable>
) :
    RecyclerView.Adapter<WeatherForecastAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View =
            LayoutInflater.from(context).inflate(R.layout.item_weather_forecast, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //Load weather icon
        holder.imgWeather.setBackgroundResource(Utils().changeWeatherImage(weatherForecastResult[position].main))
        val time: String? = weatherForecastResult[position].day.let {
            Utils().convertUnixToDay(
                it
            )
        }

        //time
        holder.txtDateTime.text = time
        //temperature
        holder.txtTemperature.text = StringBuilder(
            java.lang.String.valueOf(
                weatherForecastResult[position].temperature
            )
        ).append(" °")
    }

    override fun getItemCount(): Int {
        return weatherForecastResult.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtDateTime: TextView = itemView.findViewById(R.id.txt_date)
        var txtTemperature: TextView = itemView.findViewById(R.id.txt_temperature)
        var imgWeather: ImageView = itemView.findViewById(R.id.img_weather)
    }
}
