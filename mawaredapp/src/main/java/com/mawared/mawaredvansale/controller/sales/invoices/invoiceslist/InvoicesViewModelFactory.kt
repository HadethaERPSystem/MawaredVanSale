package com.mawared.mawaredvansale.controller.sales.invoices.invoiceslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.invoices.IInvoiceRepository

@Suppress("UNCHECKED_CAST")
class InvoicesViewModelFactory(private val repository: IInvoiceRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return InvoicesViewModel(repository) as T
    }
}