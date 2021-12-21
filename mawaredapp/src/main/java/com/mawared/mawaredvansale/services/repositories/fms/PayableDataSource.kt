package com.mawared.mawaredvansale.services.repositories.fms

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.mawared.mawaredvansale.data.db.entities.fms.Payable
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.FIRST_PAGE
import com.mawared.mawaredvansale.utilities.NoConnectivityException
import com.mawared.mawaredvansale.utilities.POST_PER_PAGE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PayableDataSource(private val api: ApiService, private val sm_Id: Int, private val  cu_Id: Int?): PageKeyedDataSource<Int, Payable>() {

    private var page = FIRST_PAGE
    val networkState: MutableLiveData<NetworkState> = MutableLiveData()

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Payable>) {
        networkState.postValue(NetworkState.LOADING)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.payable_OnPages(sm_Id, cu_Id, page, POST_PER_PAGE)
                if (response.isSuccessful) {
                    val result = response.body()!!
                    if (result.isSuccessful) {
                        if(result.data != null && result.data.isNotEmpty()){
                            val data: MutableList<Payable> = result.data as MutableList<Payable>
                            callback.onResult(data, null, page + 1)
                            networkState.postValue(NetworkState.LOADED)
                        }else{
                            networkState.postValue(NetworkState.NODATA)
                        }
                    } else {
                        networkState.postValue(NetworkState.ERROR)
                        Log.i("PayableDataSource", "${result.message}")
                    }
                } else {
                    networkState.postValue(NetworkState.ERROR)
                }
            } catch (e: ApiException) {
                networkState.postValue(NetworkState.ERROR)
                Log.e("Connectivity", "No internet connection", e)
                return@launch
            } catch (e: NoConnectivityException){
                networkState.postValue(NetworkState.ERROR_CONNECTION)
                Log.e("Connectivity", "No internet connection", e)
                return@launch
            } catch (e: Exception) {
                networkState.postValue(NetworkState.ERROR)
                Log.e("Exception", "Error exception when call getOrders", e)
                return@launch
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Payable>) {
        networkState.postValue(NetworkState.LOADING)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.payable_OnPages(sm_Id, cu_Id, params.key, POST_PER_PAGE)
                if (response.isSuccessful) {
                    val result = response.body()!!
                    if (result.isSuccessful) {
                        if(result.data != null){
                            if(result.totalPages >= params.key){
                                val data: MutableList<Payable> = result.data as MutableList<Payable>
                                callback.onResult(data, params.key + 1)
                                networkState.postValue(NetworkState.LOADED)
                            }else{
                                networkState.postValue(NetworkState.ENDOFLIST)
                            }
                        }
                    } else {
                        networkState.postValue(NetworkState.ERROR)
                        Log.i("PayableDataSource", "${result.message}")
                    }
                } else {
                    networkState.postValue(NetworkState.ERROR)
                }
            } catch (e: ApiException) {
                networkState.postValue(NetworkState.ERROR)
                Log.e("Connectivity", "No internet connection", e)
                return@launch
            } catch (e: NoConnectivityException){
                networkState.postValue(NetworkState.ERROR_CONNECTION)
                Log.e("Connectivity", "No internet connection", e)
                return@launch
            } catch (e: Exception) {
                networkState.postValue(NetworkState.ERROR)
                Log.e("Exception", "Error exception when call getOrders", e)
                return@launch
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Payable>) {

    }

    fun getNetworkState(): LiveData<NetworkState> {
        return networkState
    }
}