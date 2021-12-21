package com.mawared.mawaredvansale.services.repositories.reports.sales

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.reports.sales.SalesStatement
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.POST_PER_PAGE

class SalesRepositoryImp(private val api: ApiService): ISalesRepository, SafeApiRequest() {

    lateinit var pagedList: LiveData<PagedList<SalesStatement>>
    lateinit var salesDataSourceFactory: SalesDataSourceFactory

    override fun fetchLivePagedList(userId: Int, dtFrom: String?, dtTo: String?): LiveData<PagedList<SalesStatement>> {
        salesDataSourceFactory = SalesDataSourceFactory(api, userId, dtFrom, dtTo)


        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        pagedList = LivePagedListBuilder(salesDataSourceFactory, config).build()

        return pagedList
    }

    override fun getRecNetworkState(): LiveData<NetworkState> {
        return Transformations.switchMap<SalesDataSource, NetworkState>(salesDataSourceFactory.salesLiveDataSource, SalesDataSource::networkState)
    }

}