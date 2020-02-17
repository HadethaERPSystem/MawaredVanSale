package com.mawared.mawaredvansale.services.repositories.reports.inventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.reports.stock.StockStatement
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.POST_PER_PAGE

class InventoryRepositoryImp(private val api: ApiService): IInventroyRepository, SafeApiRequest() {

    lateinit var pagedList: LiveData<PagedList<StockStatement>>
    lateinit var invDataSourceFactory: InventoryDataSourceFactory


    override fun fetchLivePagedList(wr_Id: Int, dtFrom: String, dtTo: String): LiveData<PagedList<StockStatement>> {
        invDataSourceFactory = InventoryDataSourceFactory(api, wr_Id, dtFrom, dtTo)


        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        pagedList = LivePagedListBuilder(invDataSourceFactory, config).build()

        return pagedList
    }

    override fun getRecNetworkState(): LiveData<NetworkState> {
        return Transformations.switchMap<InventoryDataSource, NetworkState>(invDataSourceFactory.invLiveDataSource, InventoryDataSource::networkState)
    }
}