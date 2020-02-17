package com.mawared.mawaredvansale.services.repositories.reports.inventory

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.reports.stock.StockStatement
import com.mawared.mawaredvansale.services.netwrok.ApiService

class InventoryDataSourceFactory(private val api: ApiService, private val wr_Id: Int, private val dtFrom: String, private val dtTo: String): DataSource.Factory<Int, StockStatement>() {

    val invLiveDataSource = MutableLiveData<InventoryDataSource>()

    override fun create(): DataSource<Int, StockStatement> {
        val invDataSource = InventoryDataSource(api, wr_Id, dtFrom, dtTo)
        invLiveDataSource.postValue(invDataSource)
        return invDataSource
    }

    fun getMutableLiveData(): MutableLiveData<InventoryDataSource>{
        return invLiveDataSource
    }
}