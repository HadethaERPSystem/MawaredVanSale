package com.mawared.mawaredvansale.services.repositories.reports.dashboard

import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.reports.dashboard.sm_dash1
import com.mawared.mawaredvansale.data.db.entities.reports.dashboard.sm_dash2
import com.mawared.mawaredvansale.services.repositories.NetworkState


interface IDashboardRepository {
    val networkState: LiveData<NetworkState>
    fun getDashboard_TotalCustomers(sm_Id: Int, dtFrom: String, dtTo: String): LiveData<sm_dash1>
    fun getDashboard_SalesPlanning(sm_Id: Int, PlanId: Int): LiveData<sm_dash2>
    fun cancelJob()
}