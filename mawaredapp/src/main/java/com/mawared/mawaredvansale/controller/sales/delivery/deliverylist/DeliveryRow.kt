package com.mawared.mawaredvansale.controller.sales.delivery.deliverylist

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery
import com.mawared.mawaredvansale.databinding.DeliveryRowBinding
import com.xwray.groupie.databinding.BindableItem

class DeliveryRow(private val baseEo: Delivery, private val viewModel: DeliveryViewModel): BindableItem<DeliveryRowBinding>() {
    override fun getLayout() = R.layout.delivery_row

    override fun bind(viewBinding: DeliveryRowBinding, position: Int) {
        viewBinding.entityEo = baseEo
        viewBinding.viewmodel = viewModel
    }
}