package com.mawared.mawaredvansale.controller.fms.payables.payablelist

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.fms.Payable
import com.mawared.mawaredvansale.databinding.PayableRowBinding
import com.xwray.groupie.databinding.BindableItem

class PayableRow(private val baseEo: Payable, private val viewModel: PayableViewModel): BindableItem<PayableRowBinding>() {
    override fun getLayout() = R.layout.payable_row
    override fun bind(viewBinding: PayableRowBinding, position: Int) {
        viewBinding.setEntityEo(baseEo)
        viewBinding.setViewmodel(viewModel)
    }
}