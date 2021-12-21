package com.mawared.mawaredvansale.services.repositories.delivery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery_Items
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.NoConnectivityException
import com.mawared.mawaredvansale.utilities.POST_PER_PAGE
import kotlinx.coroutines.*
import java.lang.Exception

class DeliveryRepositoryImp(private val api: ApiService): IDeliveryRepository, SafeApiRequest() {
    var job: CompletableJob? = null

    lateinit var pagedList: LiveData<PagedList<Delivery>>
    lateinit var delvDataSourceFactory: DeliveryDataSourceFactory

    private val _networkState = MutableLiveData<NetworkState>()
    override val networkState: LiveData<NetworkState>
        get() = _networkState

    override fun fetchLivePagedList(sm_Id: Int, cu_Id: Int?): LiveData<PagedList<Delivery>> {
        delvDataSourceFactory = DeliveryDataSourceFactory(api, sm_Id, cu_Id)

        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        pagedList = LivePagedListBuilder(delvDataSourceFactory, config).build()

        return pagedList
    }

    override fun getDelvNetworkState(): LiveData<NetworkState> {
        return Transformations.switchMap<DeliveryDataSource, NetworkState>(delvDataSourceFactory.delvLiveDataSource, DeliveryDataSource::networkState)
    }

    override fun update1(baseEo: Delivery): LiveData<Delivery> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<Delivery>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = apiRequest { api.updateDelivery(baseEo) }
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
                            Log.e("Exception", "Error exception when call Invoice insert", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun update(baseEo: Delivery): ResponseSingle<Delivery> {
        try {
            val response = apiRequest { api.updateDelivery(baseEo) }
            return response
        }catch (e: NoConnectivityException){
            throw e
        }catch (e: ApiException){
            throw e
        }
    }

    override fun getSalesmanId(sm_Id: Int, cu_Id: Int?): LiveData<List<Delivery>> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Delivery>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = apiRequest { api.getDelivery_BySalesmanId(sm_Id, cu_Id) }
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
                            Log.e("Exception", "Error exception when call getInvoices", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getById(dl_Id: Int): LiveData<Delivery> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<Delivery>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = apiRequest { api.getDeliveryById(dl_Id) }
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
                            Log.e("Exception", "Error exception when call getInvoices", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getItemByMasterId(dl_Id: Int): LiveData<List<Delivery_Items>> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Delivery_Items>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = apiRequest { api.getAllDeliveryDetails(dl_Id) }
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
                            Log.e("Exception", "Error exception when call getItemByInvoiceId", e)
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