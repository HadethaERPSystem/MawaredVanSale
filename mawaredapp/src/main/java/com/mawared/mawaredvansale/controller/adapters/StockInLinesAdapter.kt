package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes
import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.toFormatNumber
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin_Items

import kotlinx.android.synthetic.main.stockin_item_row.view.*
import kotlinx.android.synthetic.main.stockin_item_row.view.txt_barcode
import kotlinx.android.synthetic.main.stockin_item_row.view.txt_location
import kotlinx.android.synthetic.main.stockin_item_row.view.txt_packqty
import kotlinx.android.synthetic.main.stockin_item_row.view.txt_packsize
import kotlinx.android.synthetic.main.stockin_item_row.view.txt_picker_name
import kotlinx.android.synthetic.main.stockin_item_row.view.txt_prod_name
import kotlinx.android.synthetic.main.stockin_item_row.view.txt_qty
import kotlinx.android.synthetic.main.stockin_item_row.view.txt_refno
import kotlinx.android.synthetic.main.stockout_item_row.view.*


class StockInLinesAdapter (@LayoutRes private val layoutResource: Int) : BaseAdapter<Stockin_Items>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView){
            txt_prod_name.text = item.prod_name
            txt_barcode.text = item.barcode
            txt_qty.text = "${item.qty?.toFormatNumber()}"
            txt_packsize.text = "${item.pack_size.toFormatNumber()}"
            txt_packqty.text = "${item.invQty.toFormatNumber()} ${item.uomName}"
            txt_OrderQty.text = "${item.orderQty?.toFormatNumber()}"
            txt_picker_name.text = item.picker_name
            txt_refno.text = item.baseRef
            txt_location.text = item.locName
        }
    }
}