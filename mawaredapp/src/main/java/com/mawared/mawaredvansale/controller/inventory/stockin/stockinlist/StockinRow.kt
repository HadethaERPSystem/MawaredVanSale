package com.mawared.mawaredvansale.controller.inventory.stockin.stockinlist

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin
import com.mawared.mawaredvansale.databinding.StockinRowBinding
import com.xwray.groupie.databinding.BindableItem

class StockinRow(private val baseEo: Stockin, private val viewModel: StockInViewModel): BindableItem<StockinRowBinding>() {

    override fun getLayout() = R.layout.stockin_row

    override fun bind(viewBinding: StockinRowBinding, position: Int) {
        viewBinding.entityEo = baseEo
        viewBinding.viewmodel = viewModel
    }
}
