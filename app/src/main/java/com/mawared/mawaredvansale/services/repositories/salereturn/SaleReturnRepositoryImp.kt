package com.mawared.mawaredvansale.services.repositories.salereturn

import android.util.Log
import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return_Items
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.netwrok.responses.SingleRecResponse
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.NoConnectivityException
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.lang.Exception

class SaleReturnRepositoryImp(private val api: ApiService): ISaleReturnRepository, SafeApiRequest() {
    var job: CompletableJob? = null

    override fun insert(baseEo: Sale_Return): LiveData<Sale_Return> {
        job = Job()
        return object : LiveData<Sale_Return>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.insertReturn(baseEo) }
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

    override suspend fun SaveOrUpdate(baseEo: Sale_Return): SingleRecResponse<Sale_Return> {
        try {
            val response = apiRequest { api.insertReturn(baseEo) }
            return response
        }catch (e: NoConnectivityException){
            throw e
        }catch (e: ApiException){
            throw e
        }
    }
    override fun getSaleReturn(sm_Id: Int, cu_Id: Int?): LiveData<List<Sale_Return>> {
        job = Job()
        return object : LiveData<List<Sale_Return>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getReturn(sm_Id, cu_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getSaleReturn", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getReturnById(sr_Id: Int): LiveData<Sale_Return> {
        job = Job()
        return object : LiveData<Sale_Return>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getSaleReturnById(sr_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getSaleReturn", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun delete(sr_Id: Int): LiveData<String> {
        job = Job()
        return object : LiveData<String>(){
            override fun onActive() {
                super.onActive()
                CoroutineScope(IO).launch {
                    try {
                        val response = apiRequest { api.deleteReturn(sr_Id) }
                        withContext(Main){
                            value = response.data
                            job?.complete()
                        }
                    }catch (e: ApiException){
                        Log.e("Connectivity", "No internat connection", e)
                        return@launch
                    }catch (e: Exception){
                        Log.e("Exception", "Error exception when call delete sale return", e)
                        return@launch
                    }
                }
            }
        }
    }

    override fun getItemBySaleReturnId(sr_Id: Int): LiveData<List<Sale_Return_Items>> {
        job = Job()
        return object : LiveData<List<Sale_Return_Items>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getReturnItemsByReturnId(sr_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
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