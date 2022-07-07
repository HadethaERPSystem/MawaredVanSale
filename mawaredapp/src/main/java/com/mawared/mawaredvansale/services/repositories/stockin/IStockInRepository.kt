package com.mawared.mawaredvansale.services.repositories.stockin

import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin_Items
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.services.repositories.NetworkState

interface IStockInRepository {
    val networkState: LiveData<NetworkState>
    suspend fun saveOrUpdate(baseEo: Stockin) : ResponseSingle<Stockin>
    suspend fun getStockinbyId(sin_Id: Int) : Stockin?
    suspend fun getOnpages(sm_Id: Int, term: String, page:Int) : List<Stockin>?

    fun cancelJob()
}