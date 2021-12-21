package com.mawared.mawaredvansale.controller.reports.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.reports.customer.ICuStatementRepository


@Suppress("UNCHECKED_CAST")
class CustomerStatementViewModelFactory(private val repository: ICuStatementRepository, private val masterDataRepository: IMDataRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CustomerStatementViewModel(repository, masterDataRepository) as T
    }
}