package com.mawared.mawaredvansale.services.repositories.reports.fms

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.reports.fms.CashbookStatement
import com.mawared.mawaredvansale.services.repositories.NetworkState

interface ICashbookRepository {
    fun getNetworkState(): LiveData<NetworkState>
    fun fetchLivePagedList(userId: Int, dtFrom: String?, dtTo: String?): LiveData<PagedList<CashbookStatement>>
}