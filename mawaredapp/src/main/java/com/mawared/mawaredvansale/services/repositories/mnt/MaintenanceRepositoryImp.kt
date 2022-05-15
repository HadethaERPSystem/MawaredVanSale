package com.mawared.mawaredvansale.services.repositories.mnt

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
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

    lateinit var pagedList: LiveData<PagedList<Mnts>>
    lateinit var mntDSF: MaintenanceDataSourceFactory

    private val _networkState = MutableLiveData<NetworkState>()
    override val networkState: LiveData<NetworkState>
        get() = _networkState

    override fun getOnPages(sm_Id: Int, cu_Id: Int?): LiveData<PagedList<Mnts>> {
        mntDSF = MaintenanceDataSourceFactory(api, sm_Id, cu_Id)


        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        pagedList = LivePagedListBuilder(mntDSF, config).build()

        return pagedList
    }

    override fun getSaleNetworkState(): LiveData<NetworkState> {
        return Transformations.switchMap<MaintenanceDataSource, NetworkState>(mntDSF.mntLiveDataSource, MaintenanceDataSource::networkState)
    }

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