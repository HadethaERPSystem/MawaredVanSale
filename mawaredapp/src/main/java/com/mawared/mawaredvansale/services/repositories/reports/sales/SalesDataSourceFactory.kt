package com.mawared.mawaredvansale.services.repositories.reports.sales

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.reports.sales.SalesStatement
import com.mawared.mawaredvansale.services.netwrok.ApiService

class SalesDataSourceFactory(private val api: ApiService, private val userId: Int, private val dtFrom: String?, private val dtTo: String?): DataSource.Factory<Int, SalesStatement>() {

    val salesLiveDataSource = MutableLiveData<SalesDataSource>()

    override fun create(): DataSource<Int, SalesStatement> {
        val salesDataSource = SalesDataSource(api, userId, dtFrom, dtTo)
        salesLiveDataSource.postValue(salesDataSource)
        return salesDataSource
    }

    fun getMutableLiveData(): MutableLiveData<SalesDataSource>{
        return salesLiveDataSource
    }
}