package com.mawared.mawaredvansale.services.repositories.stockin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin_Items
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.NoConnectivityException
import com.mawared.mawaredvansale.utilities.POST_PER_PAGE
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.lang.Exception

class StockinRepositoryImp(private val api: ApiService): IStockInRepository, SafeApiRequest() {

    var job: CompletableJob? = null

    private val _networkState = MutableLiveData<NetworkState>()
    override val networkState: LiveData<NetworkState>
        get() = _networkState

    override suspend fun saveOrUpdate(baseEo: Stockin): ResponseSingle<Stockin> {
        _networkState.postValue(NetworkState.LOADING)
        try {
            val response = apiRequest { api.insertStockIn(baseEo) }
            _networkState.postValue(NetworkState.LOADED)
            return response
        }catch (e: NoConnectivityException){
            _networkState.postValue(NetworkState.ERROR_CONNECTION)
            throw e
        }catch (e: ApiException){
            _networkState.postValue(NetworkState.ERROR)
            throw e
        }
    }

    override suspend fun getStockinbyId(sin_Id: Int): Stockin? {
        try {
            val response = apiRequest { api.getStockInById(sin_Id) }
            if (response.isSuccessful) {
                return response.data
            }
            return null
        }catch (e: NoConnectivityException) {
            Log.e("Connectivity", "No internat connection", e)
            return null
        }catch (e: Exception){
            Log.e("Connectivity", "Exception", e)
            return null
        }
    }

    override suspend fun getOnpages(sm_Id: Int, term: String, page: Int): List<Stockin>? {
        try {
            val response =
                apiRequest { api.getStockInOnPages(sm_Id, term, page, POST_PER_PAGE) }
            if (response.isSuccessful) {
                return response.data
            }
            return null
        } catch (e: NoConnectivityException) {
            Log.e("Connectivity", "No internat connection", e)
            return null
        }catch (e: Exception){
            Log.e("Connectivity", "Exception", e)
          return null
        }
    }

    override fun cancelJob() {
        job?.cancel()
    }
}