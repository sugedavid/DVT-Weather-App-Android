package com.dvt.weatherapp.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.dvt.weatherapp.R
import com.dvt.weatherapp.common.Common
import com.dvt.weatherapp.data.location.LocationTable
import com.dvt.weatherapp.data.location.LocationViewModel
import com.dvt.weatherapp.presentation.fragments.FavouritesFragmentDirections

class FavouritesAdapter(
    var context: Context,
    private var cities: List<LocationTable>,
    private val locationViewModel: LocationViewModel
) :
    RecyclerView.Adapter<FavouritesAdapter.MyViewHolder>() {

    var filteredLocations = cities.filter { it.isFavourite }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View =
            LayoutInflater.from(context).inflate(R.layout.item_favourite_location, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        // location name
        holder.txtLocationName.text = filteredLocations[position].cityName
        holder.txtLocationName.setOnClickListener {
            Common().saveLocationID(context, filteredLocations[position].id)
            locationViewModel.latitude.value = filteredLocations[position].latitude
            locationViewModel.longitude.value = filteredLocations[position].longitude
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
