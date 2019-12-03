package com.mawared.mawaredvansale.services.repositories.fms

import android.util.Log
import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.utilities.ApiException
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.lang.Exception

class ReceiableRepositoryImp(private val api: ApiService): IReceivableRepository, SafeApiRequest() {
    var job: CompletableJob? = null

    override fun insert(baseEo: Receivable): LiveData<Receivable> {
        job = Job()
        return object : LiveData<Receivable>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.insertReceivable(baseEo) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call insert receivable", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getReceivable(sm_Id: Int, cu_Id: Int?): LiveData<List<Receivable>> {
        job = Job()
        return object : LiveData<List<Receivable>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getReceivable(sm_Id, cu_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }

                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getReceivable", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getById(rcv_Id: Int): LiveData<Receivable> {
        job = Job()
        return object : LiveData<Receivable>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        try {
                            val response = apiRequest { api.getReceivableById(rcv_Id) }
                            withContext(Main) {
                                value = response.data
                                job?.complete()
                            }

                        }catch (e: ApiException){
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            Log.e("Exception", "Error exception when call getReceivable", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun delete(rcv_Id: Int): LiveData<String> {
        job = Job()
        return object : LiveData<String>(){
            override fun onActive() {
                super.onActive()
                CoroutineScope(IO).launch {
                    try {
                        val response = apiRequest { api.deleteReceivable(rcv_Id) }
                        withContext(Main){
                            value = response.data
                            job?.complete()
                        }
                    }catch (e: ApiException){
                        Log.e("Connectivity", "No internat connection", e)
                        return@launch
                    }catch (e: Exception){
                        Log.e("Exception", "Error exception when call delete receivable", e)
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