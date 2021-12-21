package com.mawared.mawaredvansale.controller.sales.order.addorder

import android.view.View
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order_Items
import com.mawared.mawaredvansale.databinding.OrderItemRowBinding
import com.xwray.groupie.databinding.BindableItem

class OrderItemRow(private val orderItemRow: Sale_Order_Items, private val viewModel: AddOrderViewModel) : BindableItem<OrderItemRowBinding>() {
    override fun getLayout() = R.layout.order_item_row

    override fun bind(viewBinding: OrderItemRowBinding, position: Int) {
        viewBinding.orderitem = orderItemRow
        viewBinding.viewmodel = viewModel
        viewBinding.lblBatch.visibility = if(orderItemRow.sod_pr_is_batch == "N") View.GONE else View.VISIBLE
        viewBinding.txtBatch.visibility = if(orderItemRow.sod_pr_is_batch == "N") View.GONE else View.VISIBLE
        viewBinding.lblExpiry.visibility = if(orderItemRow.sod_pr_is_batch == "N") View.GONE else View.VISIBLE
        viewBinding.txtExpirty.visibility = if(orderItemRow.sod_pr_is_batch == "N") View.GONE else View.VISIBLE
    }
}
