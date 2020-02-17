package com.mawared.mawaredvansale.services.repositories.transfer

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer
import com.mawared.mawaredvansale.services.netwrok.ApiService

class TransferDataSourceFactory(private val api: ApiService, private val userId: Int): DataSource.Factory<Int, Transfer>() {

    val transferLiveDataSource = MutableLiveData<TransferDataSource>()

    override fun create(): DataSource<Int, Transfer> {
        val trnasferDataSource = TransferDataSource(api, userId)
        transferLiveDataSource.postValue(trnasferDataSource)
        return trnasferDataSource
    }

    fun getMutableLiveData(): MutableLiveData<TransferDataSource>{
        return transferLiveDataSource
    }
}