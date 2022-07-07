package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes

import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.returnDateString
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin
import kotlinx.android.synthetic.main.stockout_row.view.*


class StockinAdapter(@LayoutRes private val layoutResource: Int, private val clickFunc1: (Stockin, String) -> Unit) : BaseAdapter<Stockin>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            txtv_doc_no.text = (item.refNo ?: "")
            txtv_doc_date.text = "${item.doc_date.toString().returnDateString()}"
            txtv_itemsno.text = "${item.docLines.count()}"
            txtv_whs_name.text = "${item.whsName}"
            txtv_status.text = "${item.invStatus}"
            txtv_bpname.text = "${item.bpName}"
            deleteBtn.tag = item
            deleteBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Stockin
                clickFunc1(item, "D")
            }

            viewBtn.tag = item
            viewBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Stockin
                clickFunc1(item, "V")
            }

            editBtn.tag = item
            editBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Stockin
                clickFunc1(item, "E")
            }
        }
    }
}

