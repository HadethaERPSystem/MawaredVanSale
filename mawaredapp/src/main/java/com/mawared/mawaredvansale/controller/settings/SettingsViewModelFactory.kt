package com.mawared.mawaredvansale.controller.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository

@Suppress("UNCHECKED_CAST")
class SettingsViewModelFactory(private val repository: IMDataRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SettingsViewModel(repository) as T
    }
}