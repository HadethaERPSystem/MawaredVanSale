package com.mawared.mawaredvansale.controller.inventory.stockout.addstockout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.stockout.IStockOutRepository

@Suppress("UNCHECKED_CAST")
class AddStockOutViewModelFactory(private val repository: IStockOutRepository, private val mdRepository: IMDataRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AddStockOutViewModel(repository, mdRepository) as T
    }
}