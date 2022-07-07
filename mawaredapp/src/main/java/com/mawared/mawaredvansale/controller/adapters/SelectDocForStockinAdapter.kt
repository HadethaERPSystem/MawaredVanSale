package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes

import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.returnDateString
import com.mawared.mawaredvansale.data.db.entities.inventory.InventoryDoc
import kotlinx.android.synthetic.main.item_rv_stockin_doc.view.*


class SelectDocForStockinAdapter(@LayoutRes private val layoutResource: Int, private val clickFunc1: (InventoryDoc) -> Unit) : BaseAdapter<InventoryDoc>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            doc_ref_no.text = (item.docRefno ?: "")
            doc_ref_date.text = "${item.doc_date?.toString()?.returnDateString()}"
            if(!item.bp_Name.isNullOrEmpty()) {
                doc_entry_name.text = "${item.bp_Name}"
            }
            doc_items_no.text = "${item.items_count}"
            doc_whs_no.text = "${item.whs_count}"
            this.tag = item
            this.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as InventoryDoc
                clickFunc1(item)
            }
        }
    }
}

