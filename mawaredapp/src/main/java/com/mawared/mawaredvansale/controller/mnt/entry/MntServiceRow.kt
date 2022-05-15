package com.mawared.mawaredvansale.controller.mnt.entry

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.mnt.MntServ
import com.mawared.mawaredvansale.databinding.MntServiceRowBinding
import com.xwray.groupie.databinding.BindableItem

class MntServiceRow(private val item: MntServ, private val viewModel: MntEntryViewModel): BindableItem<MntServiceRowBinding>() {

    override fun getLayout() = R.layout.mnt_service_row

    override fun bind(viewBinding: MntServiceRowBinding, position: Int) {
        viewBinding.baseEo = item
        viewBinding.viewmodel = viewModel
    }
}