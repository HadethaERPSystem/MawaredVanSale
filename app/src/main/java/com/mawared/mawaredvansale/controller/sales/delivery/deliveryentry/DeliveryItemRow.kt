package com.mawared.mawaredvansale.controller.sales.delivery.deliveryentry

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery_Items
import com.mawared.mawaredvansale.databinding.DeliveryItemRowBinding
import com.xwray.groupie.databinding.BindableItem

class DeliveryItemRow(private val entityEo: Delivery_Items, private val viewModel: DeliveryEntryViewModel): BindableItem<DeliveryItemRowBinding>() {
    override fun getLayout() = R.layout.delivery_item_row
    override fun bind(viewBinding: DeliveryItemRowBinding, position: Int) {
        viewBinding.entityEo = entityEo
        viewBinding.viewmodel = viewModel
    }
}