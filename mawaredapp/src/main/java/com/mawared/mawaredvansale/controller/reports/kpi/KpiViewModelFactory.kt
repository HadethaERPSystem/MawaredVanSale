package com.mawared.mawaredvansale.controller.reports.kpi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.reports.dashboard.IDashboardRepository

@Suppress("UNCHECKED_CAST")
class KpiViewModelFactory(private val repository: IDashboardRepository, private val masterDataRepository: IMDataRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return KpiViewModel(repository, masterDataRepository) as T
    }
}