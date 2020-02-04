package com.mawared.mawaredvansale.controller.sales.psorder.psorderlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.order.IOrderRepository

@Suppress("UNCHECKED_CAST")
class PSOrdersViewModelFactory(private val repository: IOrderRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PSOrdersViewModel(repository) as T
    }
}