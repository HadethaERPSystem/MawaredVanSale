package com.mawared.mawaredvansale.controller.reports.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.reports.stock.IStockRepository

@Suppress("UNCHECKED_CAST")
class StockViewModelFactory(private val repository: IStockRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return StockViewModel(repository) as T
    }
}