package com.mawared.mawaredvansale.controller.mnt.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.mnt.IMaintenanceRepository

@Suppress("UNCHECKED_CAST")
class MntEntryViewModelFactory(private val repository: IMaintenanceRepository,
                               private val masterDataRepository: IMDataRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MntEntryViewModel(repository, masterDataRepository) as T
    }
}