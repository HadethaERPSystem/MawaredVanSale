package com.mawared.mawaredvansale.services.repositories.callcycle

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.md.Call_Cycle
import com.mawared.mawaredvansale.services.repositories.NetworkState

interface ICallCycleRepository {
    val networkState: LiveData<NetworkState>

    fun saveOrUpdate(baseEo: Call_Cycle): LiveData<Call_Cycle>
    suspend fun getOnPages(sm_Id: Int, term: String, page: Int) : List<Call_Cycle>?
    fun cancelJob()
}