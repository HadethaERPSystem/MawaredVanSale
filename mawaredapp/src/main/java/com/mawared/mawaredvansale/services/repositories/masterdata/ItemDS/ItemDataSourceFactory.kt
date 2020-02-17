package com.mawared.mawaredvansale.services.repositories.masterdata.ItemDS

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.services.netwrok.ApiService

class ItemDataSourceFactory(private val api: ApiService, private val term: String, private val priceCode: String): DataSource.Factory<Int, Product>() {

    val itemLiveDataSource= MutableLiveData<ItemDataSource>()

    override fun create(): DataSource<Int, Product> {
        val itemDS = ItemDataSource(api, term, priceCode)
        itemLiveDataSource.postValue(itemDS)
        return itemDS
    }

}