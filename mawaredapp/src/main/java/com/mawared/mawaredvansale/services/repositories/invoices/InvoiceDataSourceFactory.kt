package com.mawared.mawaredvansale.services.repositories.invoices

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.services.netwrok.ApiService

class InvoiceDataSourceFactory(private val api: ApiService, private val sm_Id: Int, private val cu_Id: Int?): DataSource.Factory<Int, Sale>() {

    val saleLiveDataSource = MutableLiveData<InvoiceDataSource>()

    override fun create(): DataSource<Int, Sale> {

        val saleDataSource = InvoiceDataSource(api, sm_Id, cu_Id)
        saleLiveDataSource.postValue(saleDataSource)
        return saleDataSource
    }

    fun getMutableLiveData(): MutableLiveData<InvoiceDataSource>{
        return saleLiveDataSource
    }
}