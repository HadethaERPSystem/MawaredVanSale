package com.mawared.mawaredvansale.controller.mnt.mntlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.mnt.IMaintenanceRepository

@Suppress("UNCHECKED_CAST")
class MntsVieModelFactory(private val repository: IMaintenanceRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MntsViewModel(repository) as T
    }
}