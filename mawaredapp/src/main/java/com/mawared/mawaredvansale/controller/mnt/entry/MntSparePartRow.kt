package com.mawared.mawaredvansale.controller.mnt.entry

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.mnt.MntSpareParts
import com.mawared.mawaredvansale.databinding.MntItemRowBinding
import com.xwray.groupie.databinding.BindableItem

class MntSparePartRow(private val item: MntSpareParts, private val viewModel: MntEntryViewModel): BindableItem<MntItemRowBinding>() {
    override fun getLayout() = R.layout.mnt_item_row

    override fun bind(viewBinding: MntItemRowBinding, position: Int) {
        viewBinding.baseEo = item
        viewBinding.viewmodel = viewModel
    }
}