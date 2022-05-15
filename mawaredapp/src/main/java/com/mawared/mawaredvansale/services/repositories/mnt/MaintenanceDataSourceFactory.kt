package com.mawared.mawaredvansale.services.repositories.mnt

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.mawared.mawaredvansale.data.db.entities.mnt.Mnts
import com.mawared.mawaredvansale.services.netwrok.ApiService


class MaintenanceDataSourceFactory(private val api: ApiService, private val sm_Id: Int, private val cu_Id: Int?): DataSource.Factory<Int, Mnts>() {
    val mntLiveDataSource = MutableLiveData<MaintenanceDataSource>()

    override fun create(): DataSource<Int, Mnts> {

        val saleDataSource = MaintenanceDataSource(api, sm_Id, cu_Id)
        mntLiveDataSource.postValue(saleDataSource)
        return saleDataSource
    }

    fun getMutableLiveData(): MutableLiveData<MaintenanceDataSource> {
        return mntLiveDataSource
    }
}