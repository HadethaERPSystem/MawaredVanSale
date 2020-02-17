package com.mawared.mawaredvansale.services.repositories.salereturn

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return
import com.mawared.mawaredvansale.services.netwrok.ApiService

class ReturnDataSourceFactory(private val api: ApiService, private val sm_Id: Int, private val cu_Id: Int?): DataSource.Factory<Int, Sale_Return>() {

    val returnLiveDataSource = MutableLiveData<ReturnDataSource>()

    override fun create(): DataSource<Int, Sale_Return> {
        val returnDataSource = ReturnDataSource(api, sm_Id, cu_Id)
        returnLiveDataSource.postValue(returnDataSource)
        return returnDataSource
    }

    fun getMutableLiveData(): MutableLiveData<ReturnDataSource>{
        return returnLiveDataSource
    }
}