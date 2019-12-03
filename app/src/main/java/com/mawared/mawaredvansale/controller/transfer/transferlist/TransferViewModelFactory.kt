package com.mawared.mawaredvansale.controller.transfer.transferlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.transfer.ITransferRepository


@Suppress("UNCHECKED_CAST")
class TransferViewModelFactory(private val repository: ITransferRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TransferViewModel(repository) as T
    }
}