package com.mawared.mawaredvansale.controller.fms.receivables.receivablelist

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.databinding.ReceivableRowBinding
import com.xwray.groupie.databinding.BindableItem

class ReceivableRow(private val baseEo: Receivable, private val viewModel: ReceivableViewModel): BindableItem<ReceivableRowBinding>() {

    override fun getLayout() = R.layout.receivable_row
    override fun bind(viewBinding: ReceivableRowBinding, position: Int) {
        viewBinding.entityEo = baseEo
        viewBinding.viewmodel = viewModel
    }
}