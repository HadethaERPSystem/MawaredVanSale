package com.mawared.mawaredvansale.controller.inventory.stockin.addstockin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.stockin.IStockInRepository

@Suppress("UNCHECKED_CAST")
class AddStockInViewModelFactory(private val repository: IStockInRepository, private val mdRepository: IMDataRepository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AddStockInViewModel(repository, mdRepository) as T
    }
}