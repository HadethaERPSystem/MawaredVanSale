package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes

import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import kotlinx.android.synthetic.main.invoice_row.view.*


class InvoicesAdapter(@LayoutRes private val layoutResource: Int, private val clickFunc1: (Sale, String) -> Unit) : BaseAdapter<Sale>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            sale_doc_no.text = (item.sl_refNo ?: "")
            txtv_doc_date.text = "${item.sl_doc_date}"
            sale_customer_name.text = "${item.sl_customer_name}"
            sale_region_name.text = "${item.sl_region_name}"
            total_name.text = "${item.sl_total_amount}"
            total_dis_name.text = "${item.sl_total_discount}"
            net_name.text = "${item.sl_net_amount}"

            deleteBtn.tag = item
            deleteBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Sale
                clickFunc1(item, "D")
            }

            viewBtn.tag = item
            viewBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Sale
                clickFunc1(item, "V")
            }

            editBtn.tag = item
            editBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Sale
                clickFunc1(item, "E")
            }
            printBtn.tag = item
            printBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Sale
                clickFunc1(item, "P")
            }
        }
    }
}

