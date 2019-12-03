package com.mawared.mawaredvansale.controller.sales.invoices.addinvoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.invoices.IInvoiceRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository

@Suppress("UNCHECKED_CAST")
class AddInvoiceViewModelFactory(private val saleRepository: IInvoiceRepository,
                                 private val masterDataRepository: IMDataRepository
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AddInvoiceViewModel(
            saleRepository,
            masterDataRepository
        ) as T
    }
}