package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.LayoutRes
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import kotlinx.android.synthetic.main.customer_row.view.*


class CustomersListAdapter(@LayoutRes private val layoutResource: Int, private val permission: String, private val clickFunc1: (Customer, String) -> Unit) : BaseAdapter<Customer>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            tv_customerNo.text = (item.cu_barcode ?: "")
            cu_name.text = (item.cu_name_ar ?: "")
            tv_customer_group.text = (item.cu_payment_name ?: "")
            cu_rg_name.text = (item.cu_rg_name ?: "")
            cardView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_two))
            val perm = permission.split("|")
            editBtn.visibility = if(perm.count() > 0 && perm[3] == "1") View.VISIBLE else View.GONE
            editBtn.tag = item
            editBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Customer
                clickFunc1(item, "edit")
            }
            viewBtn.visibility = if(perm.count() > 0 && perm[2] == "1") View.VISIBLE else View.GONE
            viewBtn.tag = item
            viewBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Customer
                clickFunc1(item, "view")
            }
        }
    }
}