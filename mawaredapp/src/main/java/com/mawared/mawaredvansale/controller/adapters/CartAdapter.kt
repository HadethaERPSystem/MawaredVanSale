package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.toFormatNumber
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.data.db.entities.sales.OrderItems
import kotlinx.android.synthetic.main.item_rv_cart.view.*
import kotlinx.android.synthetic.main.item_rv_product.view.*

class CartAdapter (@LayoutRes private val layoutResource: Int, private val clickFunc1: (OrderItems) -> Unit) : BaseAdapter<OrderItems>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            prod_name.text = item.od_prod_name

            tview_qty.text = "${item.od_pack_qty?.toFormatNumber()} "
            tview_giftQty.text = "${item.od_gift_qty?.toFormatNumber()} "
            tview_batch.text = "${item.od_batch_no}"
            tview_expirty.text = "${item.od_expiry_date}"
            tview_unit_price.text = "${item.od_unit_price.toFormatNumber()}"
            tview_sc.text = App.prefs.saveUser!!.ss_cr_code!!
            tview_line_total.text = "${item.od_line_total.toFormatNumber()}"
            tview_sc2.text = App.prefs.saveUser!!.ss_cr_code!!
            tview_disc_value.text = "${item.od_disvalue.toFormatNumber()}"
            tview_net_total.text = "${item.od_net_total.toFormatNumber()}"
            tview_sc3.text = App.prefs.saveUser!!.ss_cr_code!!

            deleteBtn.tag = item
            deleteBtn.setOnClickListener{
                @Suppress("NAME_SHADOWING")
                val item = it.tag as OrderItems
                clickFunc1(item)
            }
        }
    }
}