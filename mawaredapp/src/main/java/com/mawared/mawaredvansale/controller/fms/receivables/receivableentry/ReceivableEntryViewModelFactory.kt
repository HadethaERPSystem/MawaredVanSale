package com.mawared.mawaredvansale.controller.fms.receivables.receivableentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.fms.IReceivableRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository

@Suppress("UNCHECKED_CAST")
class ReceivableEntryViewModelFactory(private val repository: IReceivableRepository, private val masterDataRepository: IMDataRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ReceivableEntryViewModel(repository, masterDataRepository) as T
    }
}