package com.mawared.mawaredvansale.services.repositories.callcycle

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.md.Call_Cycle
import com.mawared.mawaredvansale.services.netwrok.ApiService


class CallCycleDataSourceFactory(private val api: ApiService, private val sm_Id: Int, private val cu_Id: Int?): DataSource.Factory<Int, Call_Cycle>() {
    val callcycleLiveDataSource= MutableLiveData<CallCycleDataSource>()

    override fun create(): DataSource<Int, Call_Cycle> {
        val callcycleDataSource = CallCycleDataSource(api, sm_Id, cu_Id)
        callcycleLiveDataSource.postValue(callcycleDataSource)
        return callcycleDataSource
    }

    fun getMutableLiveData(): MutableLiveData<CallCycleDataSource>{
        return callcycleLiveDataSource
    }
}