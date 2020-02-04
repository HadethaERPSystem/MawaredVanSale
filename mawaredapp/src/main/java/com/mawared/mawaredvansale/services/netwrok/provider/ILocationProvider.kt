package com.mawared.mawaredvansale.services.netwrok.provider

interface ILocationProvider {
    //suspend fun hasLocationChanged(lastWeatherLocation: WeatherLocation) : Boolean
    suspend fun getPreferredLocationString() : String
}