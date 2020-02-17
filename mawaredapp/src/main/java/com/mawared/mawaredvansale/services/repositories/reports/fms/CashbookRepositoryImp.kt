package com.mawared.mawaredvansale.services.repositories.reports.fms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.reports.fms.CashbookStatement
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.POST_PER_PAGE
import java.lang.Exception

class CashbookRepositoryImp(private val api: ApiService): ICashbookRepository, SafeApiRequest() {

    lateinit var pagedList: LiveData<PagedList<CashbookStatement>>
    lateinit var cbsDataSourceFactory: CashbookDataSourceFactory



    override fun fetchLivePagedList(userId: Int, dtFrom: String?, dtTo: String?): LiveData<PagedList<CashbookStatement>> {
        cbsDataSourceFactory = CashbookDataSourceFactory(api, userId, dtFrom, dtTo)


        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        pagedList = LivePagedListBuilder(cbsDataSourceFactory, config).build()

        return pagedList
    }

    override fun getNetworkState(): LiveData<NetworkState> {
        try {
            return Transformations.switchMap<CashbookDataSource, NetworkState>(cbsDataSourceFactory.cbsLiveDataSource, CashbookDataSource::networkState)
        }catch(e: Exception){
            return MutableLiveData()
        }
    }
}