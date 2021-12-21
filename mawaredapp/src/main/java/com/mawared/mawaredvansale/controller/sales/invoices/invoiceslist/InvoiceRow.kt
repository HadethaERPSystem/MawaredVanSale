package com.mawared.mawaredvansale.controller.sales.invoices.invoiceslist

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.databinding.InvoiceRowBinding
import com.xwray.groupie.databinding.BindableItem

class InvoiceRow(private val  saleRead: Sale, private val viewModel: InvoicesViewModel): BindableItem<InvoiceRowBinding>() {

    override fun getLayout() = R.layout.invoice_row

    override fun bind(viewBinding: InvoiceRowBinding, position: Int) {
        viewBinding.sale = saleRead
        viewBinding.viewmodel = viewModel
    }
}