package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.data.db.entities.md.Product_Brand
import com.mawared.mawaredvansale.utilities.URL_IMAGE
import com.mawared.mawaredvansale.utilities.URL_IMAGE_BRAND
import kotlinx.android.synthetic.main.item_rv_brand.view.*


class BrandAdapter(@LayoutRes private val layoutResource: Int, private val clickFunc1: (Product_Brand) -> Unit) : BaseAdapter<Product_Brand>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            brandName.text = (item.br_description_ar ?: "")

            if(!item.br_image_name.isNullOrEmpty()){
                Glide.with(brandAvatar.context)
                    .asDrawable()
                    .load(URL_IMAGE_BRAND + item.br_image_name).apply(RequestOptions().fitCenter())
                    .into(brandAvatar)
            }else{
                brandAvatar.setImageDrawable(ContextCompat.getDrawable(brandAvatar.context, R.drawable.ic_brand))
            }
            this.tag = item
            this.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Product_Brand
                clickFunc1(item)
            }
        }
    }
}