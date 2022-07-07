package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes

import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.returnDateString
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout
import kotlinx.android.synthetic.main.stockout_row.view.*
import kotlinx.android.synthetic.main.stockout_row.view.txtv_doc_date
import kotlinx.android.synthetic.main.stockout_row.view.txtv_doc_no
import kotlinx.android.synthetic.main.stockout_row.view.txtv_itemsno


class StockoutAdapter(@LayoutRes private val layoutResource: Int, private val clickFunc1: (Stockout, String) -> Unit) : BaseAdapter<Stockout>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            txtv_doc_no.text = (item.refNo ?: "")
            txtv_doc_date.text = "${item.doc_date.toString().returnDateString()}"
            txtv_itemsno.text = "${item.docLines.count()}"
            txtv_whs_name.text = "${item.whsName}"
            txtv_status.text = "${item.invStatusName}"
            txtv_bpname.text = "${item.bpName}"
            deleteBtn.tag = item
            deleteBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Stockout
                clickFunc1(item, "D")
            }

            viewBtn.tag = item
            viewBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Stockout
                clickFunc1(item, "V")
            }

            editBtn.tag = item
            editBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Stockout
                clickFunc1(item, "E")
            }
        }
    }
}

