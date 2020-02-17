package com.mawared.mawaredvansale.services.repositories.masterdata.ItemDS

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.services.netwrok.ApiService

class ItemOnUserDataSourceFactory(private val api: ApiService, private val term: String, private val userId: Int, private val priceCode: String): DataSource.Factory<Int, Product>() {

    val itemLiveDataSource= MutableLiveData<ItemOnUserDataSource>()

    override fun create(): DataSource<Int, Product> {
        val itemDS = ItemOnUserDataSource(api, term, userId, priceCode)
        itemLiveDataSource.postValue(itemDS)
        return itemDS
    }

}