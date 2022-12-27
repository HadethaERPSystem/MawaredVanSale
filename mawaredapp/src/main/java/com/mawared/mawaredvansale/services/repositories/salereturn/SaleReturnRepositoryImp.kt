package com.mawared.mawaredvansale.services.repositories.salereturn

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return_Items
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.NoConnectivityException
import com.mawared.mawaredvansale.utilities.POST_PER_PAGE
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.lang.Exception

class SaleReturnRepositoryImp(private val api: ApiService): ISaleReturnRepository, SafeApiRequest() {
    var job: CompletableJob? = null

    private val _networkState = MutableLiveData<NetworkState>()
    override val networkState: LiveData<NetworkState>
        get() = _networkState

    override suspend fun SaveOrUpdate(baseEo: Sale_Return): ResponseSingle<Sale_Return> {
        _networkState.postValue(NetworkState.LOADING)
        try {
            val response = apiRequest { api.insertReturn(baseEo) }
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

    override suspend fun return_OnPages(sm_Id: Int, term: String, page: Int): List<Sale_Return>? {
        try {
            val response = apiRequest { api.return_OnPages(sm_Id, term, page, POST_PER_PAGE) }
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
        }catch (e: Exception){
            _networkState.postValue(NetworkState.LOADING)
            Log.e("Error", "Exception", e)
            return emptyList()
        }
    }

    override fun getSaleReturn(sm_Id: Int, cu_Id: Int?): LiveData<List<Sale_Return>> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Sale_Return>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getReturn(sm_Id, cu_Id) }
                            withContext(Main) {
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
                            Log.e("Exception", "Error exception when call getSaleReturn", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getReturnById(sr_Id: Int): LiveData<Sale_Return> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<Sale_Return>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getSaleReturnById(sr_Id) }
                            withContext(Main) {
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
                            Log.e("Exception", "Error exception when call getSaleReturn", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun delete(sr_Id: Int): LiveData<String> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<String>(){
            override fun onActive() {
                super.onActive()
                CoroutineScope(IO).launch {
                    try {
                        val response = apiRequest { api.deleteReturn(sr_Id) }
                        withContext(Main){
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
                        Log.e("Exception", "Error exception when call delete sale return", e)
                        return@launch
                    }
                }
            }
        }
    }

    override fun getItemBySaleReturnId(sr_Id: Int): LiveData<List<Sale_Return_Items>> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Sale_Return_Items>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getReturnItemsByReturnId(sr_Id) }
                            withContext(Main) {
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
                            Log.e("Exception", "Error exception when call getItemBySaleReturnId", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun cancelJob() {
        _networkState.value = null
        job?.cancel()
    }
}