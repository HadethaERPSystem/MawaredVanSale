package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.LayoutRes
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.returnDateString
import com.mawared.mawaredvansale.controller.helpers.extension.toFormatNumber
import com.mawared.mawaredvansale.data.db.entities.fms.Payable
import kotlinx.android.synthetic.main.payable_row.view.*


class PayableAdapter(@LayoutRes private val layoutResource: Int, private val permission: String, private val clickFunc1: (Payable, String) -> Unit) : BaseAdapter<Payable>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            doc_no.text = (item.py_ref_no ?: "")
            doc_date.text = "${item.py_doc_date.toString().returnDateString()}"
            bp_name.text = "${item.py_cu_name}"
            lbl_amount_sc.text = "${resources.getString(R.string.lbl_paid_amount)} ${item.py_cr_symbol}"
            amount_sc.text = "${item.py_amount.toFormatNumber()}"
            lbl_change_sc.text = "${resources.getString(R.string.lbl_change_amount)} ${item.py_cr_symbol}"
            change_sc.text = "${item.py_change.toFormatNumber()}"

            lbl_amount_fc.text = "${resources.getString(R.string.lbl_paid_amount)} ${item.py_lc_cr_symbol}"
            amount_fc.text = "${item.py_lc_amount.toFormatNumber()}"
            lbl_change_fc.text = "${resources.getString(R.string.lbl_change_amount)} ${item.py_lc_cr_symbol}"
            change_fc.text = "${item.py_lc_change.toFormatNumber()}"

            cardView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_one))
            val perm = permission.split("|")
            deleteBtn.visibility = View.GONE//  if(perm.count() > 0 && perm[3] == "1") View.VISIBLE else View.GONE
            deleteBtn.tag = item
            deleteBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Payable
                clickFunc1(item, "D")
            }
            viewBtn.visibility = if(perm.count() > 0 && perm[2] == "1") View.VISIBLE else View.GONE
            viewBtn.tag = item
            viewBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Payable
                clickFunc1(item, "V")
            }
            editBtn.visibility = if(perm.count() > 0 && perm[1] == "1") View.VISIBLE else View.GONE
            editBtn.tag = item
            editBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Payable
                clickFunc1(item, "E")
            }
            printBtn.visibility = if(perm.count() > 0 && perm[4] == "1") View.VISIBLE else View.GONE
            printBtn.tag = item
            printBtn.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Payable
                clickFunc1(item, "P")
            }
        }
    }
}

