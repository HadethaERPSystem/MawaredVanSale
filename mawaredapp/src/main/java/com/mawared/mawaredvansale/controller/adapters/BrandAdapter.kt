package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.data.db.entities.md.Product_Brand
import com.mawared.mawaredvansale.utilities.URL_GET_IMAGE
import kotlinx.android.synthetic.main.item_rv_brand.view.*
import java.io.FileNotFoundException


class BrandAdapter(@LayoutRes private val layoutResource: Int, private val clickFunc1: (Product_Brand) -> Unit) : BaseAdapter<Product_Brand>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            try {
                brandName.text = (item.br_description_ar ?: "")

                val options: RequestOptions = RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.imagenotfound)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH)
                    .dontAnimate()
                    .dontTransform()

                Glide.with(brandAvatar.context)
                    .load(URL_GET_IMAGE + "/Brand/" + (item.br_image_name ?: "noimage"))
                    .apply(options)
                    .into(brandAvatar)

                this.tag = item
                this.setOnClickListener {
                    @Suppress("NAME_SHADOWING")
                    val item = it.tag as Product_Brand
                    clickFunc1(item)
                }
            }catch(e: FileNotFoundException){
            }
            catch (e: Exception){
            }
        }
    }
}