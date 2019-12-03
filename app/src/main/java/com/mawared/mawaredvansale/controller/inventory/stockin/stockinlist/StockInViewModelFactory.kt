package com.mawared.mawaredvansale.controller.inventory.stockin.stockinlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.stockin.IStockInRepository

@Suppress("UNCHECKED_CAST")
class StockInViewModelFactory(private val StockInRepository: IStockInRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return StockInViewModel(StockInRepository) as T
    }
}