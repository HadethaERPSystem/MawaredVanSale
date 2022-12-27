package com.mawared.mawaredvansale.services.repositories.delivery

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery_Items
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.services.repositories.NetworkState

interface IDeliveryRepository {
    val networkState: LiveData<NetworkState>

    suspend fun SaveOrUpdate(baseEo: Delivery): ResponseSingle<Delivery>
    suspend fun get_OnPages(sm_Id: Int, term: String, page: Int): List<Delivery>?
    fun getSalesmanId(sm_Id: Int, cu_Id: Int?) : LiveData<List<Delivery>>
    fun getById(dl_Id: Int): LiveData<Delivery>
    fun getItemByMasterId(dl_Id: Int): LiveData<List<Delivery_Items>>

    fun cancelJob()
}