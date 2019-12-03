package com.mawared.mawaredvansale.services.repositories.stockout

import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout_Items
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class StockoutRepositoryImp(private val api: ApiService): IStockOutRepository, SafeApiRequest() {
    var job: CompletableJob? = null

    override fun insert(baseEo: Stockout): LiveData<Stockout> {
        job = Job()
        return object : LiveData<Stockout>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        val response = apiRequest { api.insertStockOut(baseEo) }
                        withContext(Dispatchers.Main) {
                            value = response.data
                            job?.complete()
                        }
                    }
                }
            }
        }
    }

    override fun getStockout(userId: Int): LiveData<List<Stockout>> {
        job = Job()
        return object : LiveData<List<Stockout>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        val response = apiRequest { api.getStockOutByUserId(userId) }
                        withContext(Main) {
                            value = response.data
                            job?.complete()
                        }
                    }
                }
            }
        }
    }

    override fun getItemByStockoutId(sot_Id: Int): LiveData<List<Stockout_Items>> {
        job = Job()
        return object : LiveData<List<Stockout_Items>>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(IO).launch {
                        val response = apiRequest { api.getStockOutItemsById(sot_Id) }
                        withContext(Dispatchers.Main) {
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