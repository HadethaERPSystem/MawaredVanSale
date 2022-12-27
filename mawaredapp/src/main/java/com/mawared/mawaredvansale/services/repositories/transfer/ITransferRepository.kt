package com.mawared.mawaredvansale.services.repositories.transfer

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer_Items
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.services.repositories.NetworkState

interface ITransferRepository {
    val networkState: LiveData<NetworkState>

    suspend fun saveOrUpdate(baseEo: Transfer) : ResponseSingle<Transfer>
    suspend fun get_OnPages(sm_Id: Int, term: String, page: Int): List<Transfer>?

    fun getByUserId(userId: Int) : LiveData<List<Transfer>>
    fun getById(tr_Id: Int): LiveData<Transfer>

    fun getItemsByMasterId(tr_Id: Int): LiveData<List<Transfer_Items>>

    fun cancelJob()
}