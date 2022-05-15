package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.data.db.entities.md.Product_Brand
import com.mawared.mawaredvansale.data.db.entities.md.UnitConvertion
import com.mawared.mawaredvansale.utilities.URL_IMAGE
import com.mawared.mawaredvansale.utilities.URL_IMAGE_BRAND
import kotlinx.android.synthetic.main.item_rv_brand.view.*
import kotlinx.android.synthetic.main.item_rv_uom.view.*


class UoMAdapter(@LayoutRes private val layoutResource: Int, private val clickFunc1: (UnitConvertion) -> Unit) : BaseAdapter<UnitConvertion>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            tv_uom.text = (item.uom_name ?: "")
            tv_uom_num.text = "${item.qty}"
            tv_uom_rate.text = "${item.conv_rate}"
            this.tag = item
            this.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as UnitConvertion
                clickFunc1(item)
            }
        }
    }
}