package com.mawared.mawaredvansale.controller.map

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.util.Log

object MapUtils {
    private val TAG: String = MapUtils::class.java.name

    fun isConnectedToInternet(context: Context): Boolean{
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwordk = cm.activeNetworkInfo
        val isConnected = activeNetwordk != null &&
                activeNetwordk.isConnected
        Log.i(TAG, " isConnectedToInternet $isConnected")
        return isConnected
    }

    fun isLocationServiceEnabled(context: Context): Boolean{
        var isGpsEnabled = false
        var isNetworkProviderEnabled = false

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            Log.i(TAG, " gps enabled - $isGpsEnabled")
        }catch (e: Exception){
            e.printStackTrace()
        }

        try {
            isNetworkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            Log.i(TAG, " network enabled - $isNetworkProviderEnabled")
        }catch (e: Exception){
            e.printStackTrace()
        }

        return isGpsEnabled || isNetworkProviderEnabled
    }
}