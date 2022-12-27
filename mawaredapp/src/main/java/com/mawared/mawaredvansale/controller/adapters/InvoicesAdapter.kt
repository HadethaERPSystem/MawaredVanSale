package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.LayoutRes
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.returnDateString
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import kotlinx.android.synthetic.main.invoice_row.view.*


class InvoicesAdapter(@LayoutRes private val layoutResource: Int, private val permission: String, private val clickFunc1: (Sale, String) -> Unit) : BaseAdapter<Sale>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            sale_doc_no.text = (item.sl_refNo ?: "")
            txtv_doc_date.text = "${item.sl_doc_date.toString().returnDateString()}"
            sale_customer_name.text = "${item.sl_customer_name}"
            sale_region_name.text = "${item.sl_region_name}"
            total_name.text = "${item.sl_total_amount}"
            total_dis_name.text = "${item.sl_total_discount}"
            net_name.text = "${item.sl_net_amount}"
            cardView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_one))
            val perm = permission.split("|")
            deleteBtn.visibility = View.GONE// if(perm.count() > 0 && perm[3] == "1") View.VISIBLE else View.GONE
            deleteBtn.tag = item
            deleteBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Sale
                clickFunc1(item, "D")
            }
            viewBtn.visibility = if(perm.count() > 0 && perm[2] == "1") View.VISIBLE else View.GONE
            viewBtn.tag = item
            viewBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Sale
                clickFunc1(item, "V")
            }
            editBtn.visibility = if(perm.count() > 0 && perm[1] == "1") View.VISIBLE else View.GONE
            editBtn.tag = item
            editBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Sale
                clickFunc1(item, "E")
            }
            printBtn.visibility = if(perm.count() > 0 && perm[4] == "1") View.VISIBLE else View.GONE
            printBtn.tag = item
            printBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Sale
                clickFunc1(item, "P")
            }
        }
    }
}

