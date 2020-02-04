package com.mawared.mawaredvansale.controller.sales.delivery.deliveryentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.delivery.IDeliveryRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository


@Suppress("UNCHECKED_CAST")
class DeliveryEntryViewModelFactory(private val repository: IDeliveryRepository,
                                    private val mdrepository: IMDataRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DeliveryEntryViewModel(repository, mdrepository) as T
    }
}