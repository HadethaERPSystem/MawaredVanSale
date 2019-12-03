package com.mawared.mawaredvansale.controller.inventory.stockin.addstockin

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin_Items
import com.mawared.mawaredvansale.databinding.StockinItemRowBinding
import com.xwray.groupie.databinding.BindableItem


class StockinItemRow(private val items: Stockin_Items, private val viewModel: AddStockInViewModel): BindableItem<StockinItemRowBinding>() {
    override fun getLayout() = R.layout.stockin_item_row

    override fun bind(viewBinding: StockinItemRowBinding, position: Int) {
        viewBinding.setEntityEo(items)
        viewBinding.setViewmodel(viewModel)
    }
}