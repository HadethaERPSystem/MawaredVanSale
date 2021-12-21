package com.mawared.mawaredvansale.services.repositories.reports.customer

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.reports.customer.CustomerStatement
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.POST_PER_PAGE

class CuStatementRepositoryImp(private val api: ApiService): ICuStatementRepository, SafeApiRequest() {

    lateinit var pagedList: LiveData<PagedList<CustomerStatement>>
    lateinit var salesDataSourceFactory: CuStatementDataSourceFactory

    override fun fetchLivePagedList(userId: Int, cu_Id: Int, dtFrom: String?, dtTo: String?): LiveData<PagedList<CustomerStatement>> {
        salesDataSourceFactory = CuStatementDataSourceFactory(api, userId, cu_Id, dtFrom, dtTo)


        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        pagedList = LivePagedListBuilder(salesDataSourceFactory, config).build()

        return pagedList
    }

    override fun getRecNetworkState(): LiveData<NetworkState> {
        return Transformations.switchMap<CuStatementDataSource, NetworkState>(salesDataSourceFactory.cuStatementLiveDataSource, CuStatementDataSource::networkState)
    }

}