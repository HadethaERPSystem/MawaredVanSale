package com.mawared.mawaredvansale.services.repositories.fms

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.services.repositories.NetworkState

interface IReceivableRepository {
    val networkState: LiveData<NetworkState>

    suspend fun SaveOrUpdate(baseEo: Receivable) : ResponseSingle<Receivable>
    suspend fun get_OnPages(sm_Id: Int, term: String, page: Int): List<Receivable>?
    fun getReceivable(sm_Id: Int, cu_Id: Int?) : LiveData<List<Receivable>>
    fun getById(rcv_Id: Int): LiveData<Receivable>
    fun delete(rcv_Id: Int): LiveData<String>
    
    fun cancelJob()
}