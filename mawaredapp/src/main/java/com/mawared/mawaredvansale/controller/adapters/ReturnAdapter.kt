package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.LayoutRes
import com.mawared.mawaredvansale.R

import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return
import kotlinx.android.synthetic.main.sale_return_row.view.*
import kotlinx.android.synthetic.main.sale_return_row.view.net_name
import kotlinx.android.synthetic.main.sale_return_row.view.sale_doc_no
import kotlinx.android.synthetic.main.sale_return_row.view.total_name


class ReturnAdapter(@LayoutRes private val layoutResource: Int, private val permission: String, private val clickFunc1: (Sale_Return, String) -> Unit) : BaseAdapter<Sale_Return>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            sale_doc_no.text = (item.sr_refno ?: "")
            txtv_doc_date.text = "${item.sr_doc_date}"
            sale_customer_name.text = "${item.sr_customer_name}"
            sale_region_name.text = "${item.sr_region_name}"
            total_name.text = "${item.sr_total_amount}"
            total_dis_name.text = "${item.sr_total_discount}"
            net_name.text = "${item.sr_net_amount}"
            cardView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_one))
            val perm = permission.split("|")
            deleteBtn.visibility = View.GONE// if(perm.count() > 0 && perm[3] == "1") View.VISIBLE else View.GONE
            deleteBtn.tag = item
            deleteBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Sale_Return
                clickFunc1(item, "D")
            }
            viewBtn.visibility = if(perm.count() > 2 && perm[2] == "1") View.VISIBLE else View.GONE
            viewBtn.tag = item
            viewBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Sale_Return
                clickFunc1(item, "V")
            }
            editBtn.visibility = if(perm.count() > 1 && perm[1] == "1") View.VISIBLE else View.GONE
            editBtn.tag = item
            editBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Sale_Return
                clickFunc1(item, "E")
            }
            printBtn.visibility = if(perm.count() > 4 && perm[4] == "1") View.VISIBLE else View.GONE
            printBtn.tag = item
            printBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Sale_Return
                clickFunc1(item, "P")
            }
        }
    }
}

