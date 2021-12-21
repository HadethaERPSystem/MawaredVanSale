package com.mawared.mawaredvansale.controller.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository

@Suppress("UNCHECKED_CAST")
class MapViewModelFactory(private val masterDataRepository: IMDataRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MapViewModel(masterDataRepository) as T
    }
}