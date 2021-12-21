package com.mawared.mawaredvansale.services.repositories.reports.stock

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.reports.stock.StockStatement
import com.mawared.mawaredvansale.services.netwrok.ApiService

class StockDataSourceFactory(private val api: ApiService, private val wr_Id: Int, private val dtTo: String?): DataSource.Factory<Int, StockStatement>() {

    val invLiveDataSource = MutableLiveData<StockDataSource>()

    override fun create(): DataSource<Int, StockStatement> {
        val invDataSource = StockDataSource(api, wr_Id, dtTo)
        invLiveDataSource.postValue(invDataSource)
        return invDataSource
    }

    fun getMutableLiveData(): MutableLiveData<StockDataSource>{
        return invLiveDataSource
    }
}