package com.mawared.mawaredvansale.controller.sales.salereturn.salereturnlist

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return
import com.mawared.mawaredvansale.databinding.SaleReturnRowBinding
import com.xwray.groupie.databinding.BindableItem

class SaleReturnRow(private val baseEo: Sale_Return, private val viewModel: SaleReturnViewModel): BindableItem<SaleReturnRowBinding>() {

    override fun getLayout() = R.layout.sale_return_row

    override fun bind(viewBinding: SaleReturnRowBinding, position: Int) {
        viewBinding.setEntityEo(baseEo)
        viewBinding.setViewmodel(viewModel)
    }
}