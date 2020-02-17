package com.mawared.mawaredvansale.services.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mawared.mawaredvansale.data.db.AppDatabase
import com.mawared.mawaredvansale.data.db.entities.md.Salesman
import com.mawared.mawaredvansale.data.db.entities.security.User
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.netwrok.responses.AuthResponse
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.NoConnectivityException

class UserRepository(
    private val api: ApiService,
    private val db: AppDatabase
) : SafeApiRequest() {

    private val _networkState = MutableLiveData<NetworkState>()
    val networkState: LiveData<NetworkState>
        get() = _networkState

    suspend fun userLogin(email: String, password: String): AuthResponse {
        return apiRequest { api.userLogin(email, password)}
    }

    suspend fun login(user: User) : User?{
        _networkState.postValue(NetworkState.LOADING)
        try {
            val response = apiRequest { api.login(user) }
            _networkState.postValue(NetworkState.LOADED)
            if(response.isSuccessful)
                return response.user
            else{
                _networkState.postValue(NetworkState.ERROR)
                return null

            }
        }catch (e: ApiException){
            _networkState.postValue(NetworkState.ERROR)
            return null
        }

    }

    suspend fun salesmanGet(pda_code: String): Salesman?{
        try {
            val response = apiRequest { api.salesmanGetByCode(pda_code) }
            if(response.isSuccessful) {
                return response.data
            }
            return null
        }catch (e: NoConnectivityException){
            Log.e("Connectivity", "No internat connection", e)
            return null
        }
    }

    suspend fun salesmanByUser(userId: Int): Salesman?{
        try {
            val response = apiRequest { api.salesmanGetByUser(userId) }
            if(response.isSuccessful) {
                return response.data
            }
            return null
        }catch (e: NoConnectivityException){
            Log.e("Connectivity", "No internat connection", e)
            return null
        }
    }

    suspend fun saveUser(user: User) = db.getUserDao().upsert(user)

    suspend fun updateUser(user: User) = db.getUserDao().update(user)

    suspend fun delete(user: User) = db.getUserDao().delete(user)

    fun getUser() = db.getUserDao().getUser()

    fun localUserLogin(userName: String, password: String) = db.getUserDao().userLogin(userName, password)

}