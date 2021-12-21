package com.mawared.mawaredvansale.controller.common.gpslocation

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.mawared.forecastmvvm.ui.LifecycleBoundLocationManager
import com.mawared.mawaredvansale.utilities.MY_PERMISSION_ACCESS_COARSE_LOCATION

class GpsCurrentLocation(val activity: Activity, val lifecycleOwner: LifecycleOwner): Fragment() {

    private var mCurrentLocation: Location? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null

    init {
        locationCallback()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        requestLocationPermission()
        if(hasLocationPermission()){
            bindLocationManager()
        }else{
            requestLocationPermission()
        }
    }
    private fun locationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                mCurrentLocation = p0!!.lastLocation
            }
        }
    }

    private fun bindLocationManager(){
        LifecycleBoundLocationManager(lifecycleOwner, fusedLocationProviderClient!!, mLocationCallback!!)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            MY_PERMISSION_ACCESS_COARSE_LOCATION
        )
    }

    private fun hasLocationPermission(): Boolean{
        return ContextCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == MY_PERMISSION_ACCESS_COARSE_LOCATION){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                bindLocationManager()
            else
                Toast.makeText(activity, "Please, set location manually in settings", Toast.LENGTH_LONG).show()

        }

    }

    fun getLocation(): Location?{
        return mCurrentLocation
    }
}