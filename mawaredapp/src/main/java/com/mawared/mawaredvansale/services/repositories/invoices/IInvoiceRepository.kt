package com.mawared.mawaredvansale.services.repositories.invoices

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Items
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.services.repositories.NetworkState

interface IInvoiceRepository {
    // invoice method
    val networkState: LiveData<NetworkState>
    //fun getSaleNetworkState(): LiveData<NetworkState>
    //fun fetchLivePagedList(sm_Id: Int, cu_Id: Int?): LiveData<PagedList<Sale>>

    fun insert(baseEo: Sale) : LiveData<Sale>
    suspend fun SaveOrUpdate(baseEo: Sale) : ResponseSingle<Sale>
    fun getInvoices(sm_Id: Int, cu_Id: Int?) : LiveData<List<Sale>>
    fun getInvoice(sl_Id: Int): LiveData<Sale>
    fun delete(sl_Id: Int): LiveData<String>
    suspend fun invoices_OnPages(sm_Id: Int, term: String, page: Int) : List<Sale>?
    suspend fun loadInvoice(sl_Id: Int): Sale?
    fun cancelJob()
}