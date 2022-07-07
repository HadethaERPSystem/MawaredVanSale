package com.mawared.mawaredvansale.controller.inventory.stockout.addstockout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.invoices.IInvoiceRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository

@Suppress("UNCHECKED_CAST")
class SelectInvoiceViewModelFactory (private val masterDataRepository: IMDataRepository) : ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SelectInvoiceViewModel(masterDataRepository) as T
    }
}