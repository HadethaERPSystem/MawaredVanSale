package com.mawared.mawaredvansale.controller.fms.receivables.receivablelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.fms.IReceivableRepository

@Suppress("UNCHECKED_CAST")
class ReceivableViewModelFactory(private val repository: IReceivableRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ReceivableViewModel(repository) as T
    }
}