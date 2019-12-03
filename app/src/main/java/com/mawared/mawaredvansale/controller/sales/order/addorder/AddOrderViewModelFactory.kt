package com.mawared.mawaredvansale.controller.sales.order.addorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.order.IOrderRepository

@Suppress("UNCHECKED_CAST")
class AddOrderViewModelFactory(private val repository: IOrderRepository, private val masterRepository: IMDataRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AddOrderViewModel(repository, masterRepository) as T
    }
}