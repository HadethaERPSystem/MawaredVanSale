package com.mawared.mawaredvansale.controller.reports.fms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.reports.fms.ICashbookRepository

@Suppress("UNCHECKED_CAST")
class CashbookStatementViewModelFactory(private val repository: ICashbookRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CashbookStatementViewModel(repository) as T
    }
}
