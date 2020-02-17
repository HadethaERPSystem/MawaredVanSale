package com.mawared.mawaredvansale.services.repositories.reports.fms

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.reports.fms.CashbookStatement
import com.mawared.mawaredvansale.services.netwrok.ApiService

class CashbookDataSourceFactory(private val api: ApiService, private val userId: Int,  private val dtFrom: String?, private val dtTo: String?): DataSource.Factory<Int, CashbookStatement>() {

    val cbsLiveDataSource = MutableLiveData<CashbookDataSource>()

    override fun create(): DataSource<Int, CashbookStatement> {
        val cbsDataSource = CashbookDataSource(api, userId, dtFrom, dtTo)
        cbsLiveDataSource.postValue(cbsDataSource)
        return cbsDataSource
    }

    fun getMutableLiveData(): MutableLiveData<CashbookDataSource>{
        return cbsLiveDataSource
    }
}