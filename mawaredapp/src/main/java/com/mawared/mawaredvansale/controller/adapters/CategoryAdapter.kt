package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.data.db.entities.md.Product_Category
import com.mawared.mawaredvansale.utilities.URL_IMAGE
import com.mawared.mawaredvansale.utilities.URL_IMAGE_CATEGORY
import kotlinx.android.synthetic.main.item_rv_main_category.view.*

class CategoryAdapter(@LayoutRes private val layoutResource: Int, private val clickFunc1: (Product_Category) -> Unit) : BaseAdapter<Product_Category>(null, layoutResource){

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            try {
                categoryName.text = item.pg_description_ar ?: "No Name"
                if(!item.pg_image_name.isNullOrEmpty()){
                    Glide.with(categoryAvatar.context)
                        .asDrawable()
                        .load(URL_IMAGE_CATEGORY + item.pg_image_name)
                        .into(categoryAvatar)
                }else{
                    categoryAvatar.setImageDrawable(ContextCompat.getDrawable(categoryAvatar.context, R.drawable.ic_categories))
                }

                this.tag = item
                this.setOnClickListener {
                    @Suppress("NAME_SHADOWING")
                    val item = it.tag as Product_Category
                    clickFunc1(item)
                }
            }catch (e: Exception){

            }

        }
    }
}
