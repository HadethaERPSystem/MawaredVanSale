package com.mawared.mawaredvansale.services.repositories.mnt

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mawared.mawaredvansale.data.db.entities.mnt.Mnts
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.NoConnectivityException
import com.mawared.mawaredvansale.utilities.POST_PER_PAGE
import kotlinx.coroutines.*

class MaintenanceRepositoryImp(private val api: ApiService) : IMaintenanceRepository, SafeApiRequest() {
    var job: CompletableJob? = null

    private val _networkState = MutableLiveData<NetworkState>()
    override val networkState: LiveData<NetworkState>
        get() = _networkState


    override fun getById(id: Int): LiveData<Mnts> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<Mnts>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = apiRequest { api.getMntById(id) }
                            withContext(Dispatchers.Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR_CONNECTION)
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call mnt_get_by_id", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun get_OnPages(sm_Id: Int, term: String, page: Int): List<Mnts>? {
        try {
            val response = apiRequest { api.mnts_OnPages(sm_Id, term, page, POST_PER_PAGE) }
            if(response.isSuccessful){
                return response.data
            }
            return emptyList()
        }catch (e: ApiException){
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("ApiError", "No internat connection", e)
            return emptyList()
        }
        catch (e: NoConnectivityException) {
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("Connectivity", "No internat connection", e)
            return emptyList()
        }catch (e: java.lang.Exception){
            _networkState.postValue(NetworkState.LOADING)
            Log.e("Error", "Exception", e)
            return emptyList()
        }
    }

    override suspend fun SaveOrUpdate(baseEo: Mnts): ResponseSingle<Mnts> {
        _networkState.postValue(NetworkState.LOADING)
        try {
            val response = apiRequest { api.insertMnts(baseEo) }
            _networkState.postValue(NetworkState.LOADED)
            return response
        }catch (e: NoConnectivityException){
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            throw e
        }catch (e: ApiException){
            _networkState.postValue(NetworkState.ERROR)
            throw e
        }
    }

    override fun cancelJob() {
        _networkState.value = null
        job?.cancel()
    }
}