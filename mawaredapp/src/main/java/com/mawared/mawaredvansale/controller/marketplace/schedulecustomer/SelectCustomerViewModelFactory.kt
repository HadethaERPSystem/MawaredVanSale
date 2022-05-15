package com.mawared.mawaredvansale.controller.marketplace.schedulecustomer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.OrderRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository

@Suppress("UNCHECKED_CAST")
class SelectCustomerViewModelFactory(private val repository: IMDataRepository, private val orderRepository: OrderRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SelectCustomerViewModel(repository, orderRepository) as T
    }
}