package com.mawared.mawaredvansale.services.netwrok

import android.util.Log
import com.mawared.mawaredvansale.utilities.ApiException
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response

abstract class SafeApiRequest{

    suspend fun <T: Any> apiRequest(call: suspend () -> Response<T>) : T{
        try {
            val response = call.invoke()
            if(response.isSuccessful){
                return response.body()!!
            }else{
                val error = response.errorBody()?.toString()

                val message = StringBuilder()
                error?.let {
                    try {
                        message.append(JSONObject(it).getString("message"))
                        message.append("\n")
                    }catch (e: JSONException){}

                }
                message.append("Error Code: ${response.code()}")
                throw ApiException(message.toString())
        }

        }catch (e: Exception){
            Log.e("Error API:", "${e.message}")
            throw  Exception(e.message)
        }
    }
}