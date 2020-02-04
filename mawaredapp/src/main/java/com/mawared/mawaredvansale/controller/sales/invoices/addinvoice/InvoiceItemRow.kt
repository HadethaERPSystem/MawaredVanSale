package com.mawared.mawaredvansale.controller.sales.invoices.addinvoice

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Items
import com.mawared.mawaredvansale.databinding.InvoiceItemRowBinding
import com.xwray.groupie.databinding.BindableItem

class InvoiceItemRow(private val saleItemRow: Sale_Items, private val viewModel: AddInvoiceViewModel) : BindableItem<InvoiceItemRowBinding>(){

    override fun getLayout() = R.layout.invoice_item_row

    override fun bind(viewBinding: InvoiceItemRowBinding, position: Int) {
        viewBinding.saleitem = saleItemRow
        viewBinding.viewmodel=viewModel
    }
}