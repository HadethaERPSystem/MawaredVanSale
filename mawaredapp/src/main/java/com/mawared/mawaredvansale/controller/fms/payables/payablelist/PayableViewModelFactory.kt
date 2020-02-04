package com.mawared.mawaredvansale.controller.fms.payables.payablelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.fms.IPayableRepository

@Suppress("UNCHECKED_CAST")
class PayableViewModelFactory(private val repository: IPayableRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PayableViewModel(repository) as T
    }
}