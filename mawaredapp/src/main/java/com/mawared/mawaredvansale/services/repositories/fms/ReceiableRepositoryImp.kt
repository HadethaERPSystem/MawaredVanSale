package com.mawared.mawaredvansale.services.repositories.fms

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
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

class ReceiableRepositoryImp(private val api: ApiService): IReceivableRepository, SafeApiRequest() {
    var job: CompletableJob? = null

    lateinit var receivablePagedList: LiveData<PagedList<Receivable>>
    lateinit var receivableDataSourceFactory: ReceivableDataSourceFactory

    private val _networkState = MutableLiveData<NetworkState>()
    override val networkState: LiveData<NetworkState>
        get() = _networkState


    override fun fetchLivePagedList(sm_Id: Int, cu_Id: Int?): LiveData<PagedList<Receivable>> {
        receivableDataSourceFactory = ReceivableDataSourceFactory(api, sm_Id, cu_Id)


        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        receivablePagedList = LivePagedListBuilder(receivableDataSourceFactory, config).build()

        return receivablePagedList
    }

    override fun getRecNetworkState(): LiveData<NetworkState> {
        return Transformations.switchMap<ReceivableDataSource, NetworkState>(receivableDataSourceFactory.recLiveDataSource, ReceivableDataSource::networkState)
    }

    override fun insert(baseEo: Receivable): LiveData<Receivable> {
        _networkState.postValue(NetworkState.LOADING)
        job = Job()
        return object : LiveData<Receivable>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.insertReceivable(baseEo) }
                            withContext(Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR_CONNECTION)
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call insert receivable", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun SaveOrUpdate(baseEo: Receivable): ResponseSingle<Receivable> {
        _networkState.postValue(NetworkState.LOADING)
        try {
            val response = apiRequest { api.insertReceivable(baseEo) }
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

    override fun getReceivable(sm_Id: Int, cu_Id: Int?): LiveData<List<Receivable>> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Receivable>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getReceivable(sm_Id, cu_Id) }
                            withContext(Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }

                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR_CONNECTION)
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call getReceivable", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getById(rcv_Id: Int): LiveData<Receivable> {
        _networkState.postValue(NetworkState.LOADING)
        job = Job()
        return object : LiveData<Receivable>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getReceivableById(rcv_Id) }
                            withContext(Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }

                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR_CONNECTION)
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call getReceivable", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun delete(rcv_Id: Int): LiveData<String> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<String>(){
            override fun onActive() {
                super.onActive()
                CoroutineScope(IO).launch {
                    try {
                        val response = apiRequest { api.deleteReceivable(rcv_Id) }
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
                        Log.e("Exception", "Error exception when call delete receivable", e)
                        return@launch
                    }
                }
            }
        }
    }

    override fun cancelJob() {
        job?.cancel()
    }
}