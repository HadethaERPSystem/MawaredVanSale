package com.mawared.mawaredvansale.services.repositories.invoices

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Items
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


class InvoiceRepositoryImp(private val api: ApiService): IInvoiceRepository, SafeApiRequest() {
    var job: CompletableJob? = null

    lateinit var pagedList: LiveData<PagedList<Sale>>
    lateinit var saleDataSourceFactory: InvoiceDataSourceFactory

    private val _networkState = MutableLiveData<NetworkState>()
    override val networkState: LiveData<NetworkState>
        get() = _networkState

    override fun fetchLivePagedList(sm_Id: Int, cu_Id: Int?): LiveData<PagedList<Sale>> {
        saleDataSourceFactory = InvoiceDataSourceFactory(api, sm_Id, cu_Id)


        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        pagedList = LivePagedListBuilder(saleDataSourceFactory, config).build()

        return pagedList
    }

    override fun getSaleNetworkState(): LiveData<NetworkState> {
        return Transformations.switchMap<InvoiceDataSource, NetworkState>(saleDataSourceFactory.saleLiveDataSource, InvoiceDataSource::networkState)
    }

    override fun insert(baseEo: Sale): LiveData<Sale> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<Sale>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.insertInvoice(baseEo) }
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
                            _networkState.postValue(NetworkState.LOADING)
                            Log.e("Exception", "Error exception when call Invoice insert", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun SaveOrUpdate(baseEo: Sale): ResponseSingle<Sale> {
        _networkState.postValue(NetworkState.LOADING)
        try {
            val response = apiRequest { api.insertInvoice(baseEo) }
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
    override fun getInvoices(sm_Id: Int, cu_Id: Int?): LiveData<List<Sale>> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Sale>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getInvoices(sm_Id, cu_Id) }
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
                            Log.e("Exception", "Error exception when call getInvoices", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getInvoice(sl_Id: Int): LiveData<Sale> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<Sale>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getInvoiceById(sl_Id) }
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
                            Log.e("Exception", "Error exception when call getInvoices", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun delete(sl_Id: Int): LiveData<String> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<String>(){
            override fun onActive() {
                super.onActive()
                CoroutineScope(IO).launch {
                    try {
                        val response = apiRequest { api.deleteInvoice(sl_Id) }
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
                        Log.e("Exception", "Error exception when call delete invoice", e)
                        return@launch
                    }
                }
            }
        }
    }

    override fun getItemByInvoiceId(sl_Id: Int): LiveData<List<Sale_Items>>{
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Sale_Items>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getInvoiceItemsByInvoiceId(sl_Id) }
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
                            Log.e("Exception", "Error exception when call getItemByInvoiceId", e)
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