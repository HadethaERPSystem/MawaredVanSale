package com.mawared.mawaredvansale.services.repositories.delivery

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery
import com.mawared.mawaredvansale.services.netwrok.ApiService

class DeliveryDataSourceFactory(private val api: ApiService, private val sm_Id: Int, private val cu_Id: Int?): DataSource.Factory<Int, Delivery>() {

    val delvLiveDataSource = MutableLiveData<DeliveryDataSource>()

    override fun create(): DataSource<Int, Delivery> {

        val saleDataSource = DeliveryDataSource(api, sm_Id, cu_Id)
        delvLiveDataSource.postValue(saleDataSource)
        return saleDataSource
    }

    fun getMutableLiveData(): MutableLiveData<DeliveryDataSource>{
        return delvLiveDataSource
    }
}