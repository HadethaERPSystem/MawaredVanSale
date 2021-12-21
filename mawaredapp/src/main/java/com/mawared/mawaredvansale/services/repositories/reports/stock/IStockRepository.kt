package com.mawared.mawaredvansale.services.repositories.reports.stock

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.reports.stock.StockStatement
import com.mawared.mawaredvansale.services.repositories.NetworkState

interface IStockRepository {
    val networkState: LiveData<NetworkState>
    fun getRecNetworkState(): LiveData<NetworkState>
    fun fetchLivePagedList(wr_Id: Int, dtTo: String?): LiveData<PagedList<StockStatement>>
    fun getStock(wr_Id: Int, dtTo: String?): LiveData<List<StockStatement>>
}