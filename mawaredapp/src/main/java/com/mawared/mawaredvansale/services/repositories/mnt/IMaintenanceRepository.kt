package com.mawared.mawaredvansale.services.repositories.mnt

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.mnt.Mnts
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.services.repositories.NetworkState

interface IMaintenanceRepository {
    val networkState: LiveData<NetworkState>

    suspend fun get_OnPages(sm_Id: Int, term: String, page: Int): List<Mnts>?

    fun getById(id: Int) : LiveData<Mnts>

    suspend fun SaveOrUpdate(baseEo: Mnts) : ResponseSingle<Mnts>
    fun cancelJob()
}