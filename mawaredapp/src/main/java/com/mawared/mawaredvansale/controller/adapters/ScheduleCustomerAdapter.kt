package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import kotlinx.android.synthetic.main.item_rv_customer.view.*


class ScheduleCustomerAdapter(@LayoutRes private val layoutResource: Int, private val clickFunc1: (Customer) -> Unit) : BaseAdapter<Customer>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            customerNo.text = (item.cu_barcode ?: "")
            customerName.text = (item.cu_name_ar ?: "")
            phoneNo.text = (item.cu_phone ?: "")

            this.tag = item
            this.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Customer
                clickFunc1(item)
            }
        }
    }
}