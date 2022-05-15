package com.mawared.mawaredvansale.controller.mnt.mntlist

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.mnt.Mnts

import com.mawared.mawaredvansale.databinding.MntsRowBinding
import com.xwray.groupie.databinding.BindableItem

class MntsRow(private val  baseEo: Mnts, private val viewModel: MntsViewModel): BindableItem<MntsRowBinding>() {

    override fun getLayout() = R.layout.invoice_row

    override fun bind(viewBinding: MntsRowBinding, position: Int) {
        viewBinding.baseEo = baseEo
        viewBinding.viewmodel = viewModel
    }
}