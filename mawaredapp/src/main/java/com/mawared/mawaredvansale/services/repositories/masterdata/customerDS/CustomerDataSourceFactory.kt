package com.mawared.mawaredvansale.services.repositories.masterdata.customerDS

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.services.netwrok.ApiService

class CustomerDataSourceFactory(private val api: ApiService, private val sm_Id: Int?, private val org_Id: Int?): DataSource.Factory<Int, Customer>() {

    val customerLiveDS = MutableLiveData<CustomerDataSource>()

    override fun create(): DataSource<Int, Customer> {
        val customerDataSource =
            CustomerDataSource(api, sm_Id, org_Id)
        customerLiveDS.postValue(customerDataSource)

        return customerDataSource
    }
}