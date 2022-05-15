package com.mawared.mawaredvansale.controller.mnt.entry

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.dms.Document
import com.mawared.mawaredvansale.databinding.ItemImagesRowBinding
import com.xwray.groupie.databinding.BindableItem

class DocumentRow(private val item: Document, private val viewModel: MntEntryViewModel) : BindableItem<ItemImagesRowBinding>() {
    override fun getLayout() = R.layout.item_images_row

    override fun bind(viewBinding: ItemImagesRowBinding, position: Int) {
        viewBinding.doc = item
        viewBinding.viewmodel = viewModel
    }
}