package com.mawared.mawaredvansale.controller.transfer.transferentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.transfer.ITransferRepository

@Suppress("UNCHECKED_CAST")
class TransferEntryViewModelFactory(private val repository: ITransferRepository, private val mdataRepository: IMDataRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TransferEntryViewModel(repository, mdataRepository) as T
    }
}