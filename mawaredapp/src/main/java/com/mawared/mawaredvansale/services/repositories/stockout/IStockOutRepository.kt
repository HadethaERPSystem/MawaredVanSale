package com.mawared.mawaredvansale.services.repositories.stockout

import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout_Items

interface IStockOutRepository {
    fun insert(baseEo: Stockout) : LiveData<Stockout>
    fun getStockout(userId: Int) : LiveData<List<Stockout>>
    fun getItemByStockoutId(sot_Id: Int): LiveData<List<Stockout_Items>>

    fun cancelJob()
}