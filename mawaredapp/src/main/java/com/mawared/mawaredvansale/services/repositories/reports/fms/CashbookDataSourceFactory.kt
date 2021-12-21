package com.mawared.mawaredvansale.services.repositories.reports.fms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.reports.fms.CashbookStatement
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.repositories.NetworkState

class CashbookDataSourceFactory(private val api: ApiService, private val userId: Int, private val dtFrom: String?, private val dtTo: String?): DataSource.Factory<Int, CashbookStatement>() {

    val cashbookLiveDataSource = MutableLiveData<CashbookDataSource>()

    override fun create(): DataSource<Int, CashbookStatement> {
        val tmpDataSource = CashbookDataSource(api, userId, dtFrom, dtTo)
        cashbookLiveDataSource.postValue(tmpDataSource)
        return tmpDataSource
    }

}