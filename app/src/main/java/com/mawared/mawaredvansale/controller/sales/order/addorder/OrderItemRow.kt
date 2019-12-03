package com.mawared.mawaredvansale.controller.sales.order.addorder

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order_Items
import com.mawared.mawaredvansale.databinding.OrderItemRowBinding
import com.xwray.groupie.databinding.BindableItem

class OrderItemRow(private val orderItemRow: Sale_Order_Items, private val viewModel: AddOrderViewModel) : BindableItem<OrderItemRowBinding>() {
    override fun getLayout() = R.layout.order_item_row

    override fun bind(viewBinding: OrderItemRowBinding, position: Int) {
        viewBinding.setOrderitem(orderItemRow)
        viewBinding.setViewmodel(viewModel)
    }
}
