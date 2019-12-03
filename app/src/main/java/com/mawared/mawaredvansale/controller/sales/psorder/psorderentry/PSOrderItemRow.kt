package com.mawared.mawaredvansale.controller.sales.psorder.psorderentry

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order_Items
import com.mawared.mawaredvansale.databinding.PsorderItemRowBinding
import com.xwray.groupie.databinding.BindableItem

class PSOrderItemRow(private val orderItemRow: Sale_Order_Items, private val viewModel: PSOrderEntryViewModel) : BindableItem<PsorderItemRowBinding>() {
    override fun getLayout() = R.layout.psorder_item_row

    override fun bind(viewBinding: PsorderItemRowBinding, position: Int) {
        viewBinding.setOrderitem(orderItemRow)
        viewBinding.setViewmodel(viewModel)
    }
}
