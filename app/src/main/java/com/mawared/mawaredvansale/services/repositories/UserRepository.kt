package com.mawared.mawaredvansale.services.repositories

import android.util.Log
import com.mawared.mawaredvansale.data.db.AppDatabase
import com.mawared.mawaredvansale.data.db.entities.md.Salesman
import com.mawared.mawaredvansale.data.db.entities.security.User
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.netwrok.responses.AuthResponse
import com.mawared.mawaredvansale.utilities.NoConnectivityException

class UserRepository(
    private val api: ApiService,
    private val db: AppDatabase
) : SafeApiRequest() {


    suspend fun userLogin(email: String, password: String): AuthResponse {
        return apiRequest { api.userLogin(email, password)}
    }

    suspend fun login(user: User) : AuthResponse{
        return apiRequest { api.login(user) }
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
    suspend fun saveUser(user: User) = db.getUserDao().upsert(user)

    suspend fun updateUser(user: User) = db.getUserDao().update(user)

    suspend fun delete(user: User) = db.getUserDao().delete(user)

    fun getUser() = db.getUserDao().getUser()

    fun localUserLogin(userName: String, password: String) = db.getUserDao().userLogin(userName, password)

}