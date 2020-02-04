package com.mawared.mawaredvansale.services.repositories.order


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order_Items
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

class OrderRepositoryImp(private val api: ApiService): IOrderRepository, SafeApiRequest() {

    var job: CompletableJob? = null

    lateinit var orderPagedList: LiveData<PagedList<Sale_Order>>
    lateinit var orderDataSourceFactory: OrderDataSourceFactory
    //var networkStateLiveData: LiveData<NetworkState>? = null
    override fun fetchLiveOrdersPagedList(sm_Id: Int, cu_Id: Int?, vo_code: String): LiveData<PagedList<Sale_Order>>{
        orderDataSourceFactory = OrderDataSourceFactory(api, sm_Id, cu_Id, vo_code)

        //networkStateLiveData = Transformations.switchMap<OrderDataSource, NetworkState>(orderDataSourceFactory.orderLiveDataSource, OrderDataSource::networkState)
        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        orderPagedList = LivePagedListBuilder(orderDataSourceFactory, config).build()
        return orderPagedList
    }

//    fun getNetworkState(): LiveData<NetworkState>{
//        return Transformations.switchMap<OrderDataSource, NetworkState>(orderDataSourceFactory.orderLiveDataSource, OrderDataSource::networkState)
//
//    }

    private val _networkState = MutableLiveData<NetworkState>()
    override val networkState: LiveData<NetworkState>
        get() = _networkState

    override fun getOrderNetworkState(): LiveData<NetworkState> {
        return Transformations.switchMap<OrderDataSource, NetworkState>(orderDataSourceFactory.orderLiveDataSource, OrderDataSource::networkState)
    }

    override fun insert(baseEo: Sale_Order): LiveData<Sale_Order> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<Sale_Order>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {

                            val response = apiRequest { api.insertOrder(baseEo) }
                            withContext(Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call insert sale order", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun SaveOrUpdate(baseEo: Sale_Order): ResponseSingle<Sale_Order> {
        try {
            _networkState.postValue(NetworkState.LOADING)
            val response = apiRequest { api.insertOrder(baseEo) }
            return response
        }catch (e: NoConnectivityException){
            _networkState.postValue(NetworkState.ERROR)
            throw e
        }catch (e: ApiException){
            _networkState.postValue(NetworkState.ERROR)
            throw e
        }
    }
    override fun getOrder(sm_Id: Int, cu_Id: Int?, vo_code: String): LiveData<List<Sale_Order>> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Sale_Order>>(){
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getOrders(sm_Id, cu_Id, vo_code) }
                            withContext(Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call getOrders", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getOrderOnPages(
        sm_Id: Int,
        cu_Id: Int?,
        vo_code: String,
        page: Int
    ): LiveData<List<Sale_Order>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun getOrderById(so_Id: Int): LiveData<Sale_Order> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<Sale_Order>(){
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getOrderById(so_Id) }
                            withContext(Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call getOrders", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }


    override fun delete(so_Id: Int): LiveData<String> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<String>(){
            override fun onActive() {
                super.onActive()
                CoroutineScope(IO).launch {
                    try {
                        val response = apiRequest { api.deleteOrder(so_Id) }
                        withContext(Main){
                            value = response.data
                            _networkState.postValue(NetworkState.LOADED)
                            job?.complete()
                        }
                    }catch (e: ApiException){
                        _networkState.postValue(NetworkState.ERROR)
                        Log.e("Connectivity", "No internat connection", e)
                        return@launch
                    }catch (e: Exception){
                        _networkState.postValue(NetworkState.ERROR)
                        Log.e("Exception", "Error exception when call delete order", e)
                        return@launch
                    }
                }
            }
        }
    }

    override fun getItemByOrderId(so_Id: Int): LiveData<List<Sale_Order_Items>> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<List<Sale_Order_Items>>(){
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getOrderItemsByOrderId(so_Id) }
                            withContext(Main){
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call getItemsByOrderId", e)
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