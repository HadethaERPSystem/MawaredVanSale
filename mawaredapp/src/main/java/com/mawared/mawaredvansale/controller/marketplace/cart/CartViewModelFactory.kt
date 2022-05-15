package com.mawared.mawaredvansale.controller.marketplace.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.OrderRepository
import com.mawared.mawaredvansale.services.repositories.invoices.IInvoiceRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.order.IOrderRepository

@Suppress("UNCHECKED_CAST")
class CartViewModelFactory(private val saleOrderRepository: IOrderRepository, private val saleRepository: IInvoiceRepository, private val repository: IMDataRepository, private val orderRepository: OrderRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CartViewModel(saleOrderRepository, saleRepository, repository, orderRepository) as T
    }
}