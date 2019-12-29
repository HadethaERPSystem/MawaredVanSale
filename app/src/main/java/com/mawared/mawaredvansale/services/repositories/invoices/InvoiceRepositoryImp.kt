package com.mawared.mawaredvansale.services.repositories.invoices

import android.util.Log
import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Items
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.netwrok.responses.SingleRecResponse
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.NoConnectivityException
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.lang.Exception


class InvoiceRepositoryImp(private val api: ApiService): IInvoiceRepository, SafeApiRequest() {
    var job: CompletableJob? = null

    override fun insert(baseEo: Sale): LiveData<Sale> {
        job = Job()
        return object : LiveData<Sale>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.insertInvoice(baseEo) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call Invoice insert", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun SaveOrUpdate(baseEo: Sale): SingleRecResponse<Sale> {
        try {
            val response = apiRequest { api.insertInvoice(baseEo) }
            return response
        }catch (e: NoConnectivityException){
            throw e
        }catch (e: ApiException){
            throw e
        }
    }
    override fun getInvoices(sm_Id: Int, cu_Id: Int?): LiveData<List<Sale>> {
        job = Job()
        return object : LiveData<List<Sale>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getInvoices(sm_Id, cu_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
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
        return object : LiveData<Sale>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getInvoiceById(sl_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
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
        return object : LiveData<String>(){
            override fun onActive() {
                super.onActive()
                CoroutineScope(IO).launch {
                    try {
                        val response = apiRequest { api.deleteInvoice(sl_Id) }
                        withContext(Main){
                            value = response.data
                            job?.complete()
                        }
                    }catch (e: ApiException){
                        Log.e("Connectivity", "No internet connection", e)
                        return@launch
                    }catch (e: Exception){
                        Log.e("Exception", "Error exception when call delete invoice", e)
                        return@launch
                    }
                }
            }
        }
    }

    override fun getItemByInvoiceId(sl_Id: Int): LiveData<List<Sale_Items>>{
        job = Job()
        return object : LiveData<List<Sale_Items>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getInvoiceItemsByInvoiceId(sl_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internat connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getItemByInvoiceId", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun cancelJob(){
        job?.cancel()
    }
}