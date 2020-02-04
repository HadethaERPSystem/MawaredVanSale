package com.mawared.mawaredvansale.controller.inventory.stockout.stockoutlist

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout
import com.mawared.mawaredvansale.databinding.StockoutRowBinding
import com.xwray.groupie.databinding.BindableItem

class StockoutRow(private val baseEo: Stockout, private val viewModel: StockOutViewModel): BindableItem<StockoutRowBinding>() {

    override fun getLayout() = R.layout.stockout_row

    override fun bind(viewBinding: StockoutRowBinding, position: Int) {
        viewBinding.setEntityEo(baseEo)
        viewBinding.setViewmodel(viewModel)
    }
}