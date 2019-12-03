package com.mawared.mawaredvansale.controller.home.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.MenuRepository

@Suppress("UNCHECKED_CAST")
class DashboardViewModelFactory(private val repository: MenuRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DashboardViewModel(repository) as T
    }
}