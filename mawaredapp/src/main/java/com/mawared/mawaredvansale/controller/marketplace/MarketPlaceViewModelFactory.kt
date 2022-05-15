package com.mawared.mawaredvansale.controller.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.OrderRepository

@Suppress("UNCHECKED_CAST")
class MarketPlaceViewModelFactory(private val orderRepository: OrderRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MarketPlaceViewModel(orderRepository) as T
    }
}