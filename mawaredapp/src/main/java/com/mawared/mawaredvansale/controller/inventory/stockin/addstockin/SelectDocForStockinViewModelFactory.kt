package com.mawared.mawaredvansale.controller.inventory.stockin.addstockin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository

@Suppress("UNCHECKED_CAST")
class SelectDocForStockinViewModelFactory (private val masterDataRepository: IMDataRepository) : ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SelectDocForStockinViewModel(masterDataRepository) as T
    }
}