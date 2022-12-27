package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.LayoutRes
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.returnDateString
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin
import kotlinx.android.synthetic.main.item_rv_stockin_row.view.*


class StockinAdapter(@LayoutRes private val layoutResource: Int, private val permission: String, private val clickFunc1: (Stockin, String) -> Unit) : BaseAdapter<Stockin>(null, layoutResource) {
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
            cardView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_one))
            val perm = permission.split("|")
            //deleteBtn.visibility = if(perm.count() > 0 && perm[3] == "1") View.VISIBLE else View.GONE
            deleteBtn.tag = item
            deleteBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Stockin
                clickFunc1(item, "D")
            }
            viewBtn.visibility = if(perm.count() > 0 && perm[2] == "1") View.VISIBLE else View.GONE
            viewBtn.tag = item
            viewBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Stockin
                clickFunc1(item, "V")
            }
            //editBtn.visibility = if(perm.count() > 0 && perm[1] == "1") View.VISIBLE else View.GONE
            editBtn.tag = item
            editBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Stockin
                clickFunc1(item, "E")
            }
        }
    }
}

