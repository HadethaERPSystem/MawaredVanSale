package com.mawared.mawaredvansale.services.repositories.fms

import android.util.Log
import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.fms.Payable
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.NoConnectivityException
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.lang.Exception

class PayableRepositoryImp(private val api: ApiService): IPayableRepository, SafeApiRequest() {
    var job: CompletableJob? = null

    override fun insert(baseEo: Payable): LiveData<Payable> {
        job = Job()
        return object : LiveData<Payable>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.insertPayable(baseEo) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call insert payable repository", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override suspend fun SaveOrUpdate(baseEo: Payable): ResponseSingle<Payable> {
        try {
            val response = apiRequest { api.insertPayable(baseEo) }
            return response
        }catch (e: NoConnectivityException){
            throw e
        }catch (e: ApiException){
            throw e
        }
    }

    override fun getPayable(sm_Id: Int, cu_Id: Int?): LiveData<List<Payable>> {
        job = Job()
        return object : LiveData<List<Payable>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getPayable(sm_Id, cu_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getPayable", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getById(py_Id: Int): LiveData<Payable> {
        job = Job()
        return object : LiveData<Payable>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getPayableById(py_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getPayable", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }
    override fun delete(py_Id: Int): LiveData<String> {
        job = Job()
        return object : LiveData<String>(){
            override fun onActive() {
                super.onActive()
                CoroutineScope(IO).launch {
                    try {
                        val response = apiRequest { api.deletePayable(py_Id) }
                        withContext(Main){
                            value = response.data
                            job?.complete()
                        }
                    }catch (e: ApiException){
                        Log.e("Connectivity", "No internet connection", e)
                        return@launch
                    }catch (e: Exception){
                        Log.e("Exception", "Error exception when call delete payable", e)
                        return@launch
                    }
                }
            }
        }
    }
    
    override fun cancelJob() {
        job?.cancel()
    }
}