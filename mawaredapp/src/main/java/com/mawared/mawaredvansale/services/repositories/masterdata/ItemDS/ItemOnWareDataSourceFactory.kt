package com.mawared.mawaredvansale.services.repositories.masterdata.ItemDS

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.services.netwrok.ApiService

class ItemOnWareDataSourceFactory(private val api: ApiService, private val term: String, private val wr_Id: Int, private val priceCode: String): DataSource.Factory<Int, Product>() {

    val itemLiveDataSource= MutableLiveData<ItemOnWareDataSource>()

    override fun create(): DataSource<Int, Product> {
        val itemDS = ItemOnWareDataSource(api, term, wr_Id, priceCode)
        itemLiveDataSource.postValue(itemDS)
        return itemDS
    }

}