package com.mawared.mawaredvansale.controller.reports.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.reports.sales.ISalesRepository

@Suppress("UNCHECKED_CAST")
class SalesStatementViewModelFactory(private val repository: ISalesRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SalesStatementViewModel(repository) as T
    }
}