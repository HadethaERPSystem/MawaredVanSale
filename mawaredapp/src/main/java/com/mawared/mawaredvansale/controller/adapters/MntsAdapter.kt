package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.LayoutRes
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.returnDateString
import com.mawared.mawaredvansale.controller.helpers.extension.toFormatNumber
import com.mawared.mawaredvansale.data.db.entities.mnt.Mnts
import kotlinx.android.synthetic.main.mnts_row.view.*


class MntsAdapter(@LayoutRes private val layoutResource: Int, private val permission: String, private val clickFunc1: (Mnts, String) -> Unit) : BaseAdapter<Mnts>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            doc_no.text = (item.refNo ?: "")
            doc_date.text = "${item.doc_date.toString().returnDateString()}"
            bp_name.text = "${item.cust_name}"
            device_name.text = "${item.prod_name}"
            total_name.text = "${item.totalWorkcost.toFormatNumber()}"
            total_itemcost_name.text = "${item.totalItemscost}"
            net_name.text = "${item.totalCost.toFormatNumber()}"

            cardView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_one))
            val perm = permission.split("|")
            deleteBtn.visibility = if(perm.count() > 0 && perm[3] == "1") View.VISIBLE else View.GONE
            deleteBtn.tag = item
            deleteBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Mnts
                clickFunc1(item, "D")
            }
            viewBtn.visibility = if(perm.count() > 0 && perm[2] == "1") View.VISIBLE else View.GONE
            viewBtn.tag = item
            viewBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Mnts
                clickFunc1(item, "V")
            }
            editBtn.visibility = if(perm.count() > 0 && perm[1] == "1") View.VISIBLE else View.GONE
            editBtn.tag = item
            editBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Mnts
                clickFunc1(item, "E")
            }
        }
    }
}

