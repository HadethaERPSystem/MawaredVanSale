package com.mawared.mawaredvansale.controller.sales.salereturn.salereturnentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.salereturn.ISaleReturnRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository

@Suppress("UNCHECKED_CAST")
class SaleReturnEntryViewModelFactory(private val repository: ISaleReturnRepository, private val masterDataRepository: IMDataRepository): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SaleReturnEntryViewModel(repository, masterDataRepository) as T
    }
}