package com.mawared.mawaredvansale.controller.transfer.transferentry

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer_Items
import com.mawared.mawaredvansale.databinding.TransferItemRowBinding
import com.xwray.groupie.databinding.BindableItem

class TransferItemRow(private val baseEo: Transfer_Items, private val viewModel: TransferEntryViewModel):BindableItem<TransferItemRowBinding>() {
    override fun getLayout() = R.layout.transfer_item_row

    override fun bind(viewBinding: TransferItemRowBinding, position: Int) {
        viewBinding.obj = baseEo
        viewBinding.viewmodel = viewModel
    }
}