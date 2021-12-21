package com.mawared.mawaredvansale.services.repositories.reports.customer

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.reports.customer.CustomerStatement
import com.mawared.mawaredvansale.services.repositories.NetworkState

interface ICuStatementRepository {
    fun getRecNetworkState(): LiveData<NetworkState>
    fun fetchLivePagedList(userId: Int, cu_Id: Int, dtFrom: String?, dtTo: String?): LiveData<PagedList<CustomerStatement>>
}