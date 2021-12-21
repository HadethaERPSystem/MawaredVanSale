package com.mawared.mawaredvansale.services.repositories.reports.customer

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.reports.customer.CustomerStatement
import com.mawared.mawaredvansale.services.netwrok.ApiService

class CuStatementDataSourceFactory(private val api: ApiService, private val userId: Int, private val cu_Id: Int, private val dtFrom: String?, private val dtTo: String?): DataSource.Factory<Int, CustomerStatement>() {

    val cuStatementLiveDataSource = MutableLiveData<CuStatementDataSource>()

    override fun create(): DataSource<Int, CustomerStatement> {
        val cuDataSource = CuStatementDataSource(api, userId, cu_Id, dtFrom, dtTo)
        cuStatementLiveDataSource.postValue(cuDataSource)
        return cuDataSource
    }

}