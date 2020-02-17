package com.mawared.mawaredvansale.services.repositories.transfer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer_Items
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

class TransferRepositoryImp(private val api: ApiService): ITransferRepository, SafeApiRequest() {
    var job: CompletableJob? = null

    lateinit var pagedList: LiveData<PagedList<Transfer>>
    lateinit var transferDataSourceFactory: TransferDataSourceFactory

    private val _networkState = MutableLiveData<NetworkState>()
    override val networkState: LiveData<NetworkState>
        get() = _networkState

    override fun fetchLivePagedList(userId: Int): LiveData<PagedList<Transfer>> {
        transferDataSourceFactory = TransferDataSourceFactory(api, userId)


        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        pagedList = LivePagedListBuilder(transferDataSourceFactory, config).build()

        return pagedList
    }

    override fun getPagedNetworkState(): LiveData<NetworkState> {
        return Transformations.switchMap<TransferDataSource, NetworkState>(transferDataSourceFactory.transferLiveDataSource, TransferDataSource::networkState)
    }

    override fun saveOrUpdate(baseEo: Transfer): LiveData<Transfer> {
        job = Job()
        return object : LiveData<Transfer>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.transfer_SaveOrUpdate(baseEo) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call insert sale return", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun upsert(baseEo: Transfer): ResponseSingle<Transfer> {
        try {
            val response = apiRequest { api.transfer_SaveOrUpdate(baseEo) }
            return response
        }catch (e: NoConnectivityException){
            throw e
        }catch (e: ApiException){
            throw e
        }
    }
    override fun getByUserId(userId: Int): LiveData<List<Transfer>> {
        job = Job()
        return object : LiveData<List<Transfer>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.transfer_getByUserId(userId) }
                            withContext(Dispatchers.Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: java.lang.Exception){
                            Log.e("Exception", "Error exception when call Transfer get by User Id", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getById(tr_Id: Int): LiveData<Transfer> {
        job = Job()
        return object : LiveData<Transfer>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.transfer_getById(tr_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call Transfer get by Id", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getItemsByMasterId(tr_Id: Int): LiveData<List<Transfer_Items>> {
        job = Job()
        return object : LiveData<List<Transfer_Items>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.transfer_GetItemsByMasterId(tr_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: java.lang.Exception){
                            Log.e("Exception", "Error exception when call getItemBySaleReturnId", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun cancelJob() {
        job?.cancel()
    }
}