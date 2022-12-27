package com.mawared.mawaredvansale.services.repositories.callcycle

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.md.Call_Cycle
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.NoConnectivityException
import com.mawared.mawaredvansale.utilities.POST_PER_PAGE
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.lang.Exception

class CallCycleRepositoryImp(private val api: ApiService): ICallCycleRepository, SafeApiRequest() {
    var job: CompletableJob? = null

    lateinit var pagedList: LiveData<PagedList<Call_Cycle>>

    private val _networkState = MutableLiveData<NetworkState>()
    override val networkState: LiveData<NetworkState>
        get() = _networkState


    override fun saveOrUpdate(baseEo: Call_Cycle): LiveData<Call_Cycle> {
        job = Job()
        _networkState.postValue(NetworkState.WAITING)
        return object : LiveData<Call_Cycle>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.callCycle_SaveOrUpdate(baseEo) }
                            withContext(Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.SUCCESS)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR_CONNECTION)
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call Invoice insert", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun getOnPages(sm_Id: Int, term: String, page: Int ): List<Call_Cycle>? {
        try {
            val response = apiRequest { api.call_cycle_GetByOnPages(sm_Id, term, page, POST_PER_PAGE) }
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

    override fun cancelJob(){
        _networkState.value = null
        job?.cancel()
    }
}