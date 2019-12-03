package com.mawared.mawaredvansale.controller.sales.delivery.deliverylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.delivery.IDeliveryRepository

@Suppress("UNCHECKED_CAST")
class DeliveryViewModelFactory(private val repository: IDeliveryRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DeliveryViewModel(repository) as T
    }
}