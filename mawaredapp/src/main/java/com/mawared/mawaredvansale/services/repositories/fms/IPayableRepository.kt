package com.mawared.mawaredvansale.services.repositories.fms

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.fms.Payable
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.services.repositories.NetworkState

interface IPayableRepository {
    val networkState: LiveData<NetworkState>

    suspend fun SaveOrUpdate(baseEo: Payable) : ResponseSingle<Payable>
    suspend fun get_OnPages(sm_Id: Int, term: String, page: Int): List<Payable>?
    fun getPayable(sm_Id: Int, cu_Id: Int?) : LiveData<List<Payable>>
    fun getById(py_Id: Int): LiveData<Payable>
    fun delete(py_Id: Int): LiveData<String>

    fun cancelJob()
}