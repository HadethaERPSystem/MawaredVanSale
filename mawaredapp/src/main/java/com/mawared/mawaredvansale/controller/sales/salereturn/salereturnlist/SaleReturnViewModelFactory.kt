package com.mawared.mawaredvansale.controller.sales.salereturn.salereturnlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.salereturn.ISaleReturnRepository

@Suppress("UNCHECKED_CAST")
class SaleReturnViewModelFactory(private val repository: ISaleReturnRepository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SaleReturnViewModel(repository) as T
    }
}