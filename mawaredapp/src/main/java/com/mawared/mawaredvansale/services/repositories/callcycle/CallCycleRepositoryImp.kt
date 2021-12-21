package com.mawared.mawaredvansale.services.repositories.callcycle

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.md.Call_Cycle
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.POST_PER_PAGE
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.lang.Exception

class CallCycleRepositoryImp(private val api: ApiService): ICallCycleRepository, SafeApiRequest() {
    var job: CompletableJob? = null

    lateinit var pagedList: LiveData<PagedList<Call_Cycle>>
    lateinit var callCycleDataSourceFactory: CallCycleDataSourceFactory

    private val _networkState = MutableLiveData<NetworkState>()
    override val networkState: LiveData<NetworkState>
        get() = _networkState

    override fun fetchLivePagedList(sm_Id: Int, cu_Id: Int?): LiveData<PagedList<Call_Cycle>> {
        callCycleDataSourceFactory = CallCycleDataSourceFactory(api, sm_Id, cu_Id)


        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        pagedList = LivePagedListBuilder(callCycleDataSourceFactory, config).build()

        return pagedList
    }

    override fun getCyNetworkState(): LiveData<NetworkState> {
       return Transformations.switchMap<CallCycleDataSource, NetworkState>(callCycleDataSourceFactory.callcycleLiveDataSource, CallCycleDataSource::networkState)
    }

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

    override fun cancelJob(){
        _networkState.value = null
        job?.cancel()
    }
}