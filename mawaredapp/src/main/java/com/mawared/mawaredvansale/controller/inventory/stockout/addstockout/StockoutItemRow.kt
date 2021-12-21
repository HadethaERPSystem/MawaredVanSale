package com.mawared.mawaredvansale.controller.inventory.stockout.addstockout

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout_Items
import com.mawared.mawaredvansale.databinding.StockoutItemRowBinding
import com.xwray.groupie.databinding.BindableItem

class StockoutItemRow(private val items: Stockout_Items, private val viewModel: AddStockOutViewModel): BindableItem<StockoutItemRowBinding>() {
    override fun getLayout() = R.layout.stockout_item_row

    override fun bind(viewBinding: StockoutItemRowBinding, position: Int) {
        viewBinding.entityEo = items
        viewBinding.viewmodel = viewModel
    }
}