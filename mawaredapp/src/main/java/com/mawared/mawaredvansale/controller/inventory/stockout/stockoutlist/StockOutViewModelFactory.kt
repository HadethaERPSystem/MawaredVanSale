package com.mawared.mawaredvansale.controller.inventory.stockout.stockoutlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.stockout.IStockOutRepository

@Suppress("UNCHECKED_CAST")
class StockOutViewModelFactory(private val repository: IStockOutRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return StockOutViewModel(repository) as T
    }
}