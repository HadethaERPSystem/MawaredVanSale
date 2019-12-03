package com.mawared.mawaredvansale.services.repositories.stockin

import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin_Items
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class StockinRepositoryImp(private val api: ApiService): IStockInRepository, SafeApiRequest() {

    var job: CompletableJob? = null

    override fun insert(baseEo: Stockin): LiveData<Stockin> {
        job = Job()
        return object : LiveData<Stockin>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        val response = apiRequest { api.insertStockIn(baseEo) }
                        withContext(Main) {
                            value = response.data
                            job?.complete()
                        }
                    }
                }
            }
        }
    }

    override fun getStockin(userId: Int): LiveData<List<Stockin>> {
        job = Job()
        return object : LiveData<List<Stockin>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        val response = apiRequest { api.getStockInByUserId(userId) }
                        withContext(Main) {
                            value = response.data
                            job?.complete()
                        }
                    }
                }
            }
        }
    }

    override fun getItemByStockinId(sin_Id: Int): LiveData<List<Stockin_Items>> {
        job = Job()
        return object : LiveData<List<Stockin_Items>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        val response = apiRequest { api.getStockInItemsById(sin_Id) }
                        withContext(Main) {
                            value = response.data
                            job?.complete()
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