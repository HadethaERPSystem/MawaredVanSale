package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.LayoutRes
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.returnDateString
import com.mawared.mawaredvansale.controller.helpers.extension.toFormatNumber
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order
import kotlinx.android.synthetic.main.order_row.view.*


class SalesOrderAdapter(@LayoutRes private val layoutResource: Int, private val permission: String, private val clickFunc1: (Sale_Order, String) -> Unit) : BaseAdapter<Sale_Order>(null, layoutResource)  {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val item = items[position]
        with(holder.itemView){
            order_no.text = (item.so_refNo ?: "")
            order_date.text = "${item.so_date.toString().returnDateString()}"
            bp_name.text = item.so_customer_name
            region_name.text = item.so_region_name
            total_name.text = item.so_total_amount.toFormatNumber()
            total_dis_name.text = item.so_total_discount.toFormatNumber()
            net_name.text = item.so_net_amount.toFormatNumber()
            sale_status.text = item.so_status_name
            cardView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_two))
            val perm = permission.split("|")

            deleteBtn.visibility = View.GONE// if(perm.count() > 0 && perm[3] == "1" && item.so_status_code == "Waiting") View.VISIBLE else View.GONE
            deleteBtn.tag = item
            deleteBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Sale_Order
                clickFunc1(item, "D")
            }
            viewBtn.visibility = if(perm.count() > 0 && perm[2] == "1") View.VISIBLE else View.GONE
            viewBtn.tag = item
            viewBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Sale_Order
                clickFunc1(item, "V")
            }
            editBtn.visibility = if(perm.count() > 0 && perm[1] == "1" && item.so_status_code == "Waiting") View.VISIBLE else View.GONE
            editBtn.tag = item
            editBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Sale_Order
                clickFunc1(item, "E")
            }
        }
    }
}