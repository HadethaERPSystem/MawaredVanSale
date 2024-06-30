package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.data.db.entities.md.Product_Category
import com.mawared.mawaredvansale.utilities.URL_GET_IMAGE
import kotlinx.android.synthetic.main.item_rv_main_category.view.*
import kotlinx.android.synthetic.main.item_rv_product.view.*
import java.io.FileNotFoundException

class CategoryAdapter(@LayoutRes private val layoutResource: Int, private val clickFunc1: (Product_Category) -> Unit) : BaseAdapter<Product_Category>(null, layoutResource){

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            try {
                categoryName.text = item.pg_description_ar ?: "No Name"

                val options: RequestOptions = RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.imagenotfound)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH)
                    .dontAnimate()
                    .dontTransform()

                Glide.with(this)
                    .load(URL_GET_IMAGE + "/Category/" + (if(!item.pg_image_name.isNullOrEmpty()) item.pg_image_name  else "noimage"))
                    .apply(options)
                    .into(categoryAvatar)

                this.tag = item
                this.setOnClickListener {
                    @Suppress("NAME_SHADOWING")
                    val item = it.tag as Product_Category
                    clickFunc1(item)
                }
            }
            catch(e: FileNotFoundException){
            }
            catch (e: Exception){
            }

        }
    }
}
