package com.mawared.mawaredvansale.controller.marketplace.offers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.OrderRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository

@Suppress("UNCHECKED_CAST")
class OffersViewModelFactory(private val repository: IMDataRepository, private val orderRepository: OrderRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return OffersViewModel(repository, orderRepository) as T
    }
}