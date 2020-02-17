package com.mawared.mawaredvansale.controller.sales.order.orderslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.order.IOrderRepository

@Suppress("UNCHECKED_CAST")
class OrdersViewModelFactory(private val repository: IOrderRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return OrdersViewModel(repository) as T
    }
}