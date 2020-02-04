package com.mawared.mawaredvansale.services.repositories.invoices

import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Items
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle

interface IInvoiceRepository {
    // invoice method
    fun insert(baseEo: Sale) : LiveData<Sale>
    suspend fun SaveOrUpdate(baseEo: Sale) : ResponseSingle<Sale>
    fun getInvoices(sm_Id: Int, cu_Id: Int?) : LiveData<List<Sale>>
    fun getInvoice(sl_Id: Int): LiveData<Sale>
    fun delete(sl_Id: Int): LiveData<String>
    fun getItemByInvoiceId(sl_Id: Int): LiveData<List<Sale_Items>>
    fun cancelJob()
}