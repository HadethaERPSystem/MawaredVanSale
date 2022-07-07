package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes

import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.data.db.entities.security.Menu
import kotlinx.android.synthetic.main.stockouttype_item.view.*


class StockoutTypeAdapter(@LayoutRes private val layoutResource: Int, private val clickFunc1: (Menu) -> Unit) : BaseAdapter<Menu>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            card_title.text = (item.menu_name ?: "")
            this.tag = item
            this.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val menu = it.tag as Menu
                clickFunc1(menu)
            }
        }
    }
}

