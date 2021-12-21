package com.mawared.mawaredvansale.services.repositories.reports.customer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.mawared.mawaredvansale.data.db.entities.reports.customer.CustomerStatement
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.FIRST_PAGE
import com.mawared.mawaredvansale.utilities.POST_PER_PAGE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CuStatementDataSource(private val api: ApiService, private val userId: Int, private val cu_Id: Int, private val dtFrom: String?, private val  dtTo: String?): PageKeyedDataSource<Int, CustomerStatement>() {

    private var page = FIRST_PAGE
    val networkState: MutableLiveData<NetworkState> = MutableLiveData()

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, CustomerStatement>) {
        networkState.postValue(NetworkState.LOADING)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.cusotmerStatement_OnPages(userId, cu_Id, dtFrom, dtTo, page, POST_PER_PAGE)
                if (response.isSuccessful) {
                    val result = response.body()!!
                    if (result.isSuccessful) {
                        if(result.data != null && result.data.isNotEmpty()){
                            val data: MutableList<CustomerStatement> = result.data as MutableList<CustomerStatement>
                            data.add(0, CustomerStatement(0, "Header", "", "", null,null,
                                null, null, null, null, null, null, null))
                            if(result.totalPages == 1){
                                data.add(data.size, CustomerStatement(data.size, "Footer", "", "", null,null,
                                    null, null, null, null, null, null, null))
                            }
                            callback.onResult(data, null, page + 1)
                            networkState.postValue(NetworkState.LOADED)
                        }else{
                            networkState.postValue(NetworkState.NODATA)
                        }
                    } else {
                        networkState.postValue(NetworkState.ERROR)
                        Log.i("CuStatemntDataSource", "${result.message}")
                    }
                } else {
                    networkState.postValue(NetworkState.ERROR)
                }
            } catch (e: ApiException) {
                networkState.postValue(NetworkState.ERROR)
                Log.e("Connectivity", "No internet connection", e)
                return@launch
            } catch (e: Exception) {
                networkState.postValue(NetworkState.ERROR)
                Log.e("Exception", "Error exception when call getOrders", e)
                return@launch
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, CustomerStatement>) {
        networkState.postValue(NetworkState.LOADING)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.cusotmerStatement_OnPages(userId, cu_Id, dtFrom, dtTo, params.key, POST_PER_PAGE)
                if (response.isSuccessful) {
                    val result = response.body()!!
                    if (result.isSuccessful) {
                        if(result.data != null){
                            if(result.totalPages >= params.key){
                                val data: MutableList<CustomerStatement> = result.data as MutableList<CustomerStatement>
                                if(result.totalPages < params.key +1){
                                    data.add(data.size,CustomerStatement(data.size, "Footer", "", "", null,null,
                                        null, null, null, null, null, null, null ))
                                }
                                callback.onResult(data, params.key + 1)
                                networkState.postValue(NetworkState.LOADED)
                            }else{
                                networkState.postValue(NetworkState.ENDOFLIST)
                            }
                        }
                    } else {
                        networkState.postValue(NetworkState.ERROR)
                        Log.i("cuStatementDataSource", "${result.message}")
                    }
                } else {
                    networkState.postValue(NetworkState.ERROR)
                }
            } catch (e: ApiException) {
                networkState.postValue(NetworkState.ERROR)
                Log.e("Connectivity", "No internet connection", e)
                return@launch
            } catch (e: Exception) {
                networkState.postValue(NetworkState.ERROR)
                Log.e("Exception", "Error exception when call getOrders", e)
                return@launch
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, CustomerStatement>) {

    }

    fun getNetworkState(): LiveData<NetworkState> {
        return networkState
    }
}