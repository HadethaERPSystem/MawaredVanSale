package com.mawared.mawaredvansale.services.repositories.order


import android.util.Log
import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order_Items
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.netwrok.responses.SingleRecResponse
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.NoConnectivityException
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.lang.Exception

class OrderRepositoryImp(private val api: ApiService): IOrderRepository, SafeApiRequest() {

    var job: CompletableJob? = null

    override fun insert(baseEo: Sale_Order): LiveData<Sale_Order> {
        job = Job()
        return object : LiveData<Sale_Order>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.insertOrder(baseEo) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call insert sale order", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun SaveOrUpdate(baseEo: Sale_Order): SingleRecResponse<Sale_Order> {
        try {
            val response = apiRequest { api.insertOrder(baseEo) }
            return response
        }catch (e: NoConnectivityException){
            throw e
        }catch (e: ApiException){
            throw e
        }
    }
    override fun getOrder(sm_Id: Int, cu_Id: Int?, vo_code: String): LiveData<List<Sale_Order>> {
        job = Job()
        return object : LiveData<List<Sale_Order>>(){
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getOrders(sm_Id, cu_Id, vo_code) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getOrders", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getOrderById(so_Id: Int): LiveData<Sale_Order> {
        job = Job()
        return object : LiveData<Sale_Order>(){
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getOrderById(so_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
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
        return object : LiveData<String>(){
            override fun onActive() {
                super.onActive()
                CoroutineScope(IO).launch {
                    try {
                        val response = apiRequest { api.deleteOrder(so_Id) }
                        withContext(Main){
                            value = response.data
                            job?.complete()
                        }
                    }catch (e: ApiException){
                        Log.e("Connectivity", "No internat connection", e)
                        return@launch
                    }catch (e: Exception){
                        Log.e("Exception", "Error exception when call delete order", e)
                        return@launch
                    }
                }
            }
        }
    }

    override fun getItemByOrderId(so_Id: Int): LiveData<List<Sale_Order_Items>> {
        job = Job()
        return object : LiveData<List<Sale_Order_Items>>(){
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getOrderItemsByOrderId(so_Id) }
                            withContext(Main){
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
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