package com.mawared.mawaredvansale.controller.home.reportsdashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.MenuRepository

@Suppress("UNCHECKED_CAST")
class ReportViewModelFactory(private val repository: MenuRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ReportsViewModel(repository) as T
    }
}