package com.mawared.mawaredvansale.services.repositories.reports.inventory

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.reports.stock.StockStatement
import com.mawared.mawaredvansale.services.repositories.NetworkState

interface IInventroyRepository {
    fun getRecNetworkState(): LiveData<NetworkState>
    fun fetchLivePagedList(wr_Id: Int, dtFrom: String, dtTo: String): LiveData<PagedList<StockStatement>>
}