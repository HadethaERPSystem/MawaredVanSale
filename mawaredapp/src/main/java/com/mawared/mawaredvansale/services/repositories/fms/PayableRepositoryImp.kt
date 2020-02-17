package com.mawared.mawaredvansale.services.repositories.fms

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.fms.Payable
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

class PayableRepositoryImp(private val api: ApiService): IPayableRepository, SafeApiRequest() {
    var job: CompletableJob? = null

    lateinit var payablePagedList: LiveData<PagedList<Payable>>
    lateinit var payableDataSourceFactory: PayableDataSourceFactory

    private val _networkState = MutableLiveData<NetworkState>()
    override val networkState: LiveData<NetworkState>
        get() = _networkState

    override fun fetchLivePagedList(sm_Id: Int, cu_Id: Int?): LiveData<PagedList<Payable>> {
        payableDataSourceFactory = PayableDataSourceFactory(api, sm_Id, cu_Id)


        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        payablePagedList = LivePagedListBuilder(payableDataSourceFactory, config).build()

        return payablePagedList
    }

    override fun getPayableNetworkState(): LiveData<NetworkState> {
        return Transformations.switchMap<PayableDataSource, NetworkState>(payableDataSourceFactory.payableLiveDataSource, PayableDataSource::networkState)
    }

    override fun insert(baseEo: Payable): LiveData<Payable> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<Payable>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.insertPayable(baseEo) }
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
                            Log.e("Exception", "Error exception when call insert payable repository", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun SaveOrUpdate(baseEo: Payable): ResponseSingle<Payable> {
        _networkState.postValue(NetworkState.LOADING)
        try {
            val response = apiRequest { api.insertPayable(baseEo) }
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

    override fun getPayable(sm_Id: Int, cu_Id: Int?): LiveData<List<Payable>> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Payable>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getPayable(sm_Id, cu_Id) }
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
                            Log.e("Exception", "Error exception when call getPayable", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getById(py_Id: Int): LiveData<Payable> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<Payable>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getPayableById(py_Id) }
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
                            Log.e("Exception", "Error exception when call getPayable", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }
    override fun delete(py_Id: Int): LiveData<String> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<String>(){
            override fun onActive() {
                super.onActive()
                CoroutineScope(IO).launch {
                    try {
                        val response = apiRequest { api.deletePayable(py_Id) }
                        withContext(Main){
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
                        Log.e("Exception", "Error exception when call delete payable", e)
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