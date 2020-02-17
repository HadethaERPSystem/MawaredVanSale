package com.mawared.mawaredvansale.services.repositories.fms

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.services.netwrok.ApiService

class ReceivableDataSourceFactory(private val api: ApiService, private val sm_Id: Int, private val cu_Id: Int?): DataSource.Factory<Int, Receivable>() {

    val recLiveDataSource = MutableLiveData<ReceivableDataSource>()

    override fun create(): DataSource<Int, Receivable> {
        val recDataSource = ReceivableDataSource(api, sm_Id, cu_Id)
        recLiveDataSource.postValue(recDataSource)
        return recDataSource
    }

    fun getMutableLiveData(): MutableLiveData<ReceivableDataSource>{
        return recLiveDataSource
    }
}