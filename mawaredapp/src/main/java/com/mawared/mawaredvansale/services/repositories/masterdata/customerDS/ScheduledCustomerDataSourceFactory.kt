package com.mawared.mawaredvansale.services.repositories.masterdata.customerDS

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.services.netwrok.ApiService

class ScheduledCustomerDataSourceFactory(private val api: ApiService, private val sm_Id: Int): DataSource.Factory<Int, Customer>() {

    val schCustomerLiveDS = MutableLiveData<ScheduledCustomerDataSource>()

    override fun create(): DataSource<Int, Customer> {
        val schCustomerDS =
            ScheduledCustomerDataSource(api, sm_Id)
        schCustomerLiveDS.postValue(schCustomerDS)

        return schCustomerDS
    }
}