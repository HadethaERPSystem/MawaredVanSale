package com.mawared.mawaredvansale.services.repositories.reports.stock

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.data.db.entities.reports.stock.StockStatement
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.POST_PER_PAGE
import kotlinx.coroutines.*
import java.lang.Exception

class StockRepositoryImp(private val api: ApiService): IStockRepository, SafeApiRequest() {
    var job: CompletableJob? = null

    lateinit var pagedList: LiveData<PagedList<StockStatement>>
    lateinit var invDataSourceFactory: StockDataSourceFactory

    private val _networkState = MutableLiveData<NetworkState>()
    override val networkState: LiveData<NetworkState>
        get() =  _networkState

    override fun fetchLivePagedList(wr_Id: Int, dtTo: String?): LiveData<PagedList<StockStatement>> {
        invDataSourceFactory = StockDataSourceFactory(api, wr_Id, dtTo)


        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        pagedList = LivePagedListBuilder(invDataSourceFactory, config).build()

        return pagedList
    }

    override fun getRecNetworkState(): LiveData<NetworkState> {
        return Transformations.switchMap<StockDataSource, NetworkState>(invDataSourceFactory.invLiveDataSource, StockDataSource::networkState)
    }

    override fun getStock(wr_Id: Int, dtTo: String?): LiveData<List<StockStatement>> {
        //api.inventoryStatement_OnPages(wr_Id, dtTo, page, POST_PER_PAGE)
        job = Job()
        return object : LiveData<List<StockStatement>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response =
                                apiRequest { api.inventoryStatement_OnPages(wr_Id, dtTo, 1, 1000) }
                            withContext(Dispatchers.Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getProducts", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }
}