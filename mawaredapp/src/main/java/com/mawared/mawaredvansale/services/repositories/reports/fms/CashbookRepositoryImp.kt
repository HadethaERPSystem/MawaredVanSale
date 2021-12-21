package com.mawared.mawaredvansale.services.repositories.reports.fms

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.reports.fms.CashbookStatement
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.POST_PER_PAGE

class CashbookRepositoryImp(private val api: ApiService): ICashbookRepository {

    lateinit var pagedListData: LiveData<PagedList<CashbookStatement>>
    lateinit var cashbookDataSourceFactory: CashbookDataSourceFactory

    override fun fetchData(userId: Int, dtFrom: String?, dtTo: String?): LiveData<PagedList<CashbookStatement>> {
        cashbookDataSourceFactory = CashbookDataSourceFactory(api, userId, dtFrom, dtTo)
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        pagedListData = LivePagedListBuilder(cashbookDataSourceFactory, config).build()

        return pagedListData
    }

    override fun getCashNetworkState(): LiveData<NetworkState> {
        return Transformations.switchMap<CashbookDataSource, NetworkState>(cashbookDataSourceFactory.cashbookLiveDataSource, CashbookDataSource::networkState)
    }

}