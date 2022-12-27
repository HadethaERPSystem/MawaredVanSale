package com.mawared.mawaredvansale.services.repositories.reports.customer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.reports.customer.CustomerStatement
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.NoConnectivityException
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

    private val _networkState = MutableLiveData<NetworkState>()
    override val networkState: LiveData<NetworkState>
        get() = _networkState

    override suspend fun get_OnPages(userId: Int, cu_Id: Int, dtFrom: String?, dtTo: String?, page: Int): List<CustomerStatement>? {
        try {
            val response = apiRequest { api.cusotmerStatement_OnPages(userId, cu_Id, dtFrom, dtTo, page, POST_PER_PAGE) }
            if(response.isSuccessful){
                return response.data
            }
            return emptyList()
        }catch (e: ApiException){
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("ApiError", "No internat connection", e)
            return emptyList()
        }
        catch (e: NoConnectivityException) {
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            Log.e("Connectivity", "No internat connection", e)
            return emptyList()
        }catch (e: java.lang.Exception){
            _networkState.postValue(NetworkState.LOADING)
            Log.e("Error", "Exception", e)
            return emptyList()
        }
    }
}