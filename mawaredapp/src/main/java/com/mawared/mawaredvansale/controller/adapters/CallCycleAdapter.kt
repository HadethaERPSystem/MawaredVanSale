package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.getColor
import com.mawared.mawaredvansale.controller.helpers.extension.returnDateString
import com.mawared.mawaredvansale.data.db.entities.md.Call_Cycle
import kotlinx.android.synthetic.main.call_cycle_row.view.*
import kotlinx.android.synthetic.main.order_row.view.editBtn

class CallCycleAdapter(@LayoutRes private val layoutResource: Int, private val clickFunc1: (Call_Cycle) -> Unit) : BaseAdapter<Call_Cycle>(null, layoutResource)  {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val item = items[position]
        with(holder.itemView){
            view_status.setBackgroundColor(item.cy_color_code.getColor())
            status_name.text = (item.cy_status_name ?: "")
            doc_date.text = "${item.cy_date.toString().returnDateString()}"
            cy_bp_code.text = item.cy_cu_code
            cy_bp_name.text = item.cy_cu_name

            editBtn.tag = item
            editBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Call_Cycle
                clickFunc1(item)
            }

        }
    }
}