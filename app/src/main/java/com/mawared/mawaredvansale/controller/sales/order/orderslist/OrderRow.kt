package com.mawared.mawaredvansale.controller.sales.order.orderslist

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order
import com.mawared.mawaredvansale.databinding.OrderRowBinding
import com.xwray.groupie.databinding.BindableItem

class OrderRow(private val  order: Sale_Order, private val viewModel: OrdersViewModel): BindableItem<OrderRowBinding>() {

    override fun getLayout() = R.layout.order_row

    override fun bind(viewBinding: OrderRowBinding, position: Int) {
        viewBinding.setOrder(order)
        viewBinding.setViewmodel(viewModel)
    }
}