package com.mawared.mawaredvansale.services.repositories.callcycle

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.md.Call_Cycle
import com.mawared.mawaredvansale.services.repositories.NetworkState

interface ICallCycleRepository {
    val networkState: LiveData<NetworkState>
    fun getCyNetworkState(): LiveData<NetworkState>
    fun fetchLivePagedList(sm_Id: Int, cu_Id: Int?): LiveData<PagedList<Call_Cycle>>

    fun saveOrUpdate(baseEo: Call_Cycle): LiveData<Call_Cycle>
    fun cancelJob()
}