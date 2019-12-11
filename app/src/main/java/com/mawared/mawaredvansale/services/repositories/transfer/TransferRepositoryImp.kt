package com.mawared.mawaredvansale.services.repositories.transfer

import android.util.Log
import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer_Items
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.utilities.ApiException
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class TransferRepositoryImp(private val api: ApiService): ITransferRepository, SafeApiRequest() {
    var job: CompletableJob? = null

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