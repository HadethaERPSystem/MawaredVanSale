package com.mawared.mawaredvansale.controller.md.customerlist

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.databinding.CustomerRowBinding
import com.xwray.groupie.databinding.BindableItem

class CustomerRow(private val baseEo: Customer, private val viewModel: CustomerViewModel): BindableItem<CustomerRowBinding>() {
    override fun getLayout() = R.layout.customer_row

    override fun bind(viewBinding: CustomerRowBinding, position: Int) {
        viewBinding.entityEo = baseEo
        viewBinding.viewmodel = viewModel
    }
}