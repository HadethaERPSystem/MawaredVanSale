package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes

import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.toFormatNumber
import com.mawared.mawaredvansale.data.db.entities.md.Loc
import kotlinx.android.synthetic.main.item_lv_locations.view.*


class ItemSelectedLocsAdapter(@LayoutRes private val layoutResource: Int, private val clickFunc1: (Loc) -> Unit) : BaseAdapter<Loc>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            loc_name.text = (item.loc_name ?: "")
            loc_qty.text = "${item.qty?.toFormatNumber()}"

            this.tag = item
            this.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Loc
                clickFunc1(item)
            }
        }
    }
}

