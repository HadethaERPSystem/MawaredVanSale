package com.mawared.mawaredvansale.services.repositories.fms

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.fms.Payable
import com.mawared.mawaredvansale.services.netwrok.ApiService

class PayableDataSourceFactory(private val api: ApiService, private val sm_Id: Int, private val cu_Id: Int?): DataSource.Factory<Int, Payable>() {

    val payableLiveDataSource = MutableLiveData<PayableDataSource>()

    override fun create(): DataSource<Int, Payable> {
        val payableDataSource = PayableDataSource(api, sm_Id, cu_Id)
        payableLiveDataSource.postValue(payableDataSource)
        return payableDataSource
    }

    fun getMutableLiveData(): MutableLiveData<PayableDataSource>{
        return payableLiveDataSource
    }
}