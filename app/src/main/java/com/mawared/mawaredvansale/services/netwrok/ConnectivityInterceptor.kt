package com.mawared.mawaredvansale.services.netwrok

import android.content.Context
import android.net.ConnectivityManager
import com.mawared.mawaredvansale.utilities.NO_CONNECTION_AVAILABLE
import com.mawared.mawaredvansale.utilities.NoConnectivityException
import okhttp3.Interceptor
import okhttp3.Response

class ConnectivityInterceptor(context: Context) : Interceptor {

    private val appContext = context.applicationContext

    override fun intercept(chain: Interceptor.Chain): Response {
            if(!isOnline())
            throw NoConnectivityException(NO_CONNECTION_AVAILABLE)
        else
            return chain.proceed(chain.request())
    }

    private fun isOnline() : Boolean{
        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}