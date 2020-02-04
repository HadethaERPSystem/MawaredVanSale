package com.mawared.mawaredvansale.services.repositories.stockin

import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin_Items

interface IStockInRepository {
    fun insert(baseEo: Stockin) : LiveData<Stockin>
    fun getStockin(userId: Int) : LiveData<List<Stockin>>
    fun getItemByStockinId(sin_Id: Int): LiveData<List<Stockin_Items>>

    fun cancelJob()
}