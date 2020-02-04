package com.mawared.mawaredvansale.services.repositories.order

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order
import com.mawared.mawaredvansale.services.netwrok.ApiService

class OrderDataSourceFactory(private val api: ApiService, private val sm_Id: Int, private val cu_Id: Int?, private val vo_code: String): DataSource.Factory<Int, Sale_Order>() {

    val orderLiveDataSource = MutableLiveData<OrderDataSource>()

    override fun create(): DataSource<Int, Sale_Order> {
        val orderDataSource = OrderDataSource(api, sm_Id, cu_Id, vo_code)
        orderLiveDataSource.postValue(orderDataSource)
        return orderDataSource
    }
}