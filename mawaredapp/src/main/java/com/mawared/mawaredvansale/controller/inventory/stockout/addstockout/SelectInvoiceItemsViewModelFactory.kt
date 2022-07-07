package com.mawared.mawaredvansale.controller.inventory.stockout.addstockout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.invoices.IInvoiceRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.stockout.IStockOutRepository

@Suppress("UNCHECKED_CAST")
class SelectInvoiceItemsViewModelFactory (private val stockService: IStockOutRepository, private val masterDataRepository: IMDataRepository) : ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SelectInvoiceItemsViewModel(stockService, masterDataRepository) as T
    }
}