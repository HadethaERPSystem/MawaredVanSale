package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.LayoutRes
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.returnDateString
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery
import kotlinx.android.synthetic.main.delivery_row.view.*


class DeliveryAdapter(@LayoutRes private val layoutResource: Int, private val permission: String, private val clickFunc1: (Delivery, String) -> Unit) : BaseAdapter<Delivery>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            doc_no.text = (item.dl_refNo ?: "")
            doc_date.text = "${item.dl_doc_date.toString().returnDateString()}"
            bp_name.text = "${item.dl_customer_name}"
            region_name.text = "${item.dl_region_name}"

            cardView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_one))
            val perm = permission.split("|")

            viewBtn.visibility = if(perm.count() > 0 && perm[2] == "1") View.VISIBLE else View.GONE
            viewBtn.tag = item
            viewBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Delivery
                clickFunc1(item, "V")
            }
            editBtn.visibility = if(perm.count() > 0 && perm[1] == "1" && item.dl_isDelivered == "W") View.VISIBLE else View.GONE
            editBtn.tag = item
            editBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Delivery
                clickFunc1(item, "E")
            }
            printBtn.visibility = if(perm.count() > 0 && perm[4] == "1") View.VISIBLE else View.GONE
            printBtn.tag = item
            printBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Delivery
                clickFunc1(item, "P")
            }
        }
    }
}

