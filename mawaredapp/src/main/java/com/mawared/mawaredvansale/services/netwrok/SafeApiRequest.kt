package com.mawared.mawaredvansale.services.netwrok

import android.util.Log
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.NoConnectivityException
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response

abstract class SafeApiRequest {

    suspend fun <T : Any> apiRequest(call: suspend () -> Response<T>): T {
        try {
            val response = call.invoke()
            if (response.isSuccessful) {
                return response.body()!!
            } else {
                val error = response.errorBody()?.toString()

                val message = StringBuilder()
                error?.let {
                    try {
                        message.append(JSONObject(it).getString("message"))
                        message.append("\n")
                    } catch (e: JSONException) {
                    }

                }
                message.append("Error Code: ${response.code()}")
                throw ApiException(message.toString())
            }

        } catch (e: NoConnectivityException){
            Log.e("Connectivity", "No internet connection", e)
            throw NoConnectivityException(e.message!!)
        }  catch (e: Exception) {
            Log.e("Error API:", "${e.message}")
            throw  Exception(e.message)
        }catch (e: ApiException){
            throw ApiException(e.message!!)
        }

    }
}