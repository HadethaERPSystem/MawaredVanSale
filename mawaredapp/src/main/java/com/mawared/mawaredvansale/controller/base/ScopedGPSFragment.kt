package com.mawared.mawaredvansale.controller.base

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import java.util.*

abstract class ScopedGPSFragment: Fragment() {
    //Now we need to create some varaibales that we will need
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var currentLocation: Location

    // the permission id is just an int that must be unique so you can use any number
    private var PERMISSION_ID = 52

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Now let's intiate the fused..providerclient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLastLocation()
    }
    fun getUserLocation(): Location{
        return currentLocation
    }

    //Now we will create a function that will allow us to get the last location
    @SuppressLint("MissingPermission")
    fun getLastLocation(){
        // first we check permission
        if(CheckPermission()){
            //Now we check the location service is enabled
            if(isLocationEnabled()){
                //Now let's get the Location
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if(location == null){
                        //if the location is null we will get the new user location
                        //so we need to create a new funtion
                        //don't forget to add the new location function
                        getNewLocation()
                    }else{
                        //location.latitude will return the latitude coordinates
                        //location.longitude will return the longitude coordinates
                        currentLocation = location
                    }
                }
            }else{
                Toast.makeText(requireContext(), "Please Enable your Location service", Toast.LENGTH_SHORT).show()
            }
        }else{
            RequestPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getNewLocation(){
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval =0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,locationCallback, Looper.myLooper()!!
            //Now let's create the locationCallback variable
        )
    }

    private val locationCallback = object  : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            val lastLocation: Location = p0.lastLocation
            currentLocation = lastLocation
        }
    }

    // First we need to create a function that will check the uses permission
    private fun CheckPermission():Boolean{
        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED
        ){
            return true
        }

        return false
    }

    // Now we need to create a funciton that will allow us to get user permission
    private fun RequestPermission(){
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_ID)
    }

    //function to get the city name
    fun getCityName(lat:Double, long:Double): String{
        var CityName = ""
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val address = geoCoder.getFromLocation(lat, long, 1)

        CityName = address?.get(0)?.locality ?: ""
        return CityName
    }

    //function to get the country name
    fun getCountryName(lat:Double, long:Double): String{
        var countryName = ""
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val address = geoCoder.getFromLocation(lat, long, 1)

        countryName = address?.get(0)?.countryName ?: ""
        return countryName
    }

    // Now we need a function that check if the location service of the device is enabled
    private fun isLocationEnabled(): Boolean{
        val locationManager: LocationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // this is a built-in function that check the permission result
        // we will use it just for debugging our code
        if(requestCode == PERMISSION_ID){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Debag", "You have the permission")
            }
        }
    }
}