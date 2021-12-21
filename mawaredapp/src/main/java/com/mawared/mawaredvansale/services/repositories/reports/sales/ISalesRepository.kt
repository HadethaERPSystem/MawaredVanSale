package com.mawared.mawaredvansale.services.repositories.reports.sales

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.reports.sales.SalesStatement
import com.mawared.mawaredvansale.services.repositories.NetworkState

interface ISalesRepository {
    fun getRecNetworkState(): LiveData<NetworkState>
    fun fetchLivePagedList(userId: Int, dtFrom: String?, dtTo: String?): LiveData<PagedList<SalesStatement>>
}