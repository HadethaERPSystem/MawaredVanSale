package com.mawared.mawaredvansale.controller.sales.salereturn.salereturnentry

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return_Items
import com.mawared.mawaredvansale.databinding.SaleReturnItemRowBinding
import com.xwray.groupie.databinding.BindableItem

class SaleReturnItemRow(private val baseEo: Sale_Return_Items, private val viewModel: SaleReturnEntryViewModel): BindableItem<SaleReturnItemRowBinding>(){

    override fun getLayout() = R.layout.sale_return_item_row

    override fun bind(viewBinding: SaleReturnItemRowBinding, position: Int) {
        viewBinding.entityEo = baseEo
        viewBinding.viewmodel = viewModel
    }
}