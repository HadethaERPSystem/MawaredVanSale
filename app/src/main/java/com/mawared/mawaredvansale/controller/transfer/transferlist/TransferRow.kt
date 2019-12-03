package com.mawared.mawaredvansale.controller.transfer.transferlist

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer
import com.mawared.mawaredvansale.databinding.TransferRowBinding
import com.xwray.groupie.databinding.BindableItem

class TransferRow(private val baseEo: Transfer, private val viewModel: TransferViewModel): BindableItem<TransferRowBinding>() {
    override fun getLayout() = R.layout.transfer_row
    override fun bind(viewBinding: TransferRowBinding, position: Int) {
        viewBinding.viewmodel = viewModel
        viewBinding.entityEo = baseEo
    }
}