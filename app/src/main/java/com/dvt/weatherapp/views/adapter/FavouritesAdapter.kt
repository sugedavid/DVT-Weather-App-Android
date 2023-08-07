package com.dvt.weatherapp.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.dvt.weatherapp.R
import com.dvt.weatherapp.data.utils.Utils
import com.dvt.weatherapp.data.room.enitities.CurrentWeatherTable
import com.dvt.weatherapp.data.view_model.WeatherViewModel
import com.dvt.weatherapp.views.fragments.FavouritesFragmentDirections

class FavouritesAdapter(
    var context: Context,
    private var cities: List<CurrentWeatherTable>,
    private val weatherViewModel: WeatherViewModel
) :
    RecyclerView.Adapter<FavouritesAdapter.MyViewHolder>() {

    var filteredLocations = cities.filter { it.isFavourite }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View =
            LayoutInflater.from(context).inflate(R.layout.item_favourite_location, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val lat = filteredLocations[position].latitude
        val lng = filteredLocations[position].longitude
        val isFavourite = filteredLocations[position].isFavourite

        // location name
        holder.txtLocationName.text = filteredLocations[position].cityName
        holder.txtLocationName.setOnClickListener {
//            Utils().saveLocationID(context, filteredLocations[position].id)
            weatherViewModel.latitude.value = lat
            weatherViewModel.longitude.value = lng
            weatherViewModel.fetchWeatherWorker(lat, lng, isFavourite)
            val action = FavouritesFragmentDirections.actionFavouritesFragmentToHomeFragment()
            holder.itemView.findNavController().navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return filteredLocations.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtLocationName: TextView = itemView.findViewById(R.id.txt_location_name)

    }
}
