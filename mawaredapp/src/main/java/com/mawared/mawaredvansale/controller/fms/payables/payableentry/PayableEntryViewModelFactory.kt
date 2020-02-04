package com.mawared.mawaredvansale.controller.fms.payables.payableentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.fms.IPayableRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository

@Suppress("UNCHECKED_CAST")
class PayableEntryViewModelFactory(private val repository: IPayableRepository, private val masterDataRepository: IMDataRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PayableEntryViewModel(repository, masterDataRepository) as T
    }
}