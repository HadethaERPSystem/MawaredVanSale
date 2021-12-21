package com.mawared.mawaredvansale.controller.sales.psorder.psorderlist

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order
import com.mawared.mawaredvansale.databinding.PsorderRowBinding
import com.xwray.groupie.databinding.BindableItem

class PSOrderRow(private val  order: Sale_Order, private val viewModel: PSOrdersViewModel): BindableItem<PsorderRowBinding>() {

    override fun getLayout() = R.layout.psorder_row

    override fun bind(viewBinding: PsorderRowBinding, position: Int) {
        viewBinding.order = order
        viewBinding.viewmodel = viewModel
    }
}