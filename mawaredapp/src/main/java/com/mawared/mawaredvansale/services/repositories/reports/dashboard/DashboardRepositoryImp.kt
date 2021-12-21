package com.mawared.mawaredvansale.services.repositories.reports.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mawared.mawaredvansale.data.db.AppDatabase
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.reports.dashboard.sm_dash1
import com.mawared.mawaredvansale.data.db.entities.reports.dashboard.sm_dash2
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.ApiException
import kotlinx.coroutines.*


class DashboardRepositoryImp(private val api: ApiService): IDashboardRepository, SafeApiRequest() {
    var job: CompletableJob? = null
    private val _networkState = MutableLiveData<NetworkState>()
    override val networkState: LiveData<NetworkState>
        get() = _networkState

    override fun getDashboard_TotalCustomers(sm_Id: Int, dtFrom: String, dtTo: String ): LiveData<sm_dash1> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<sm_dash1>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = apiRequest { api.getDashboardTotCustomer(sm_Id, dtFrom, dtTo) }
                            withContext(Dispatchers.Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call getCustomers", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun getDashboard_SalesPlanning(sm_Id: Int, PlanId: Int): LiveData<sm_dash2> {
        job = Job()
        _networkState.postValue(NetworkState.LOADING)
        return object : LiveData<sm_dash2>() {
            override fun onActive() {
                super.onActive()
                job?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = apiRequest { api.getDashboardSalesPlanning(sm_Id, PlanId) }
                            withContext(Dispatchers.Main) {
                                value = response.data
                                _networkState.postValue(NetworkState.LOADED)
                                job?.complete()
                            }
                        }catch (e: ApiException){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Connectivity", "No internet connection", e)
                            return@launch
                        }catch (e: Exception){
                            _networkState.postValue(NetworkState.ERROR)
                            Log.e("Exception", "Error exception when call getCustomers", e)
                            return@launch
                        }
                    }
                }
            }
        }
    }

    override fun cancelJob(){
        job?.cancel()
    }
}