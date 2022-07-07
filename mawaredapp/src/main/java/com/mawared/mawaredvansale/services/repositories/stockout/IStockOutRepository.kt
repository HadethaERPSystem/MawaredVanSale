package com.mawared.mawaredvansale.services.repositories.stockout

import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout_Items
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.services.repositories.NetworkState

interface IStockOutRepository {
    val networkState: LiveData<NetworkState>
    suspend fun saveOrUpdate(baseEo: Stockout) : ResponseSingle<Stockout>
    suspend fun getStockoutById(sot_Id: Int) : Stockout?
    suspend fun getOnPages(m_Id: Int, term: String, page:Int): List<Stockout>?

    fun cancelJob()
}