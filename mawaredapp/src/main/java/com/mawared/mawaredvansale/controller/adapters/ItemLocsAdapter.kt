package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import androidx.annotation.LayoutRes

import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.toFormatNumber
import com.mawared.mawaredvansale.data.db.entities.inventory.InventoryDocLines
import com.mawared.mawaredvansale.data.db.entities.md.Loc
import kotlinx.android.synthetic.main.item_lv_locations.view.*
import kotlinx.android.synthetic.main.item_lv_locations.view.addBtn
import kotlinx.android.synthetic.main.item_lv_locations.view.addQty
import kotlinx.android.synthetic.main.item_lv_locations.view.minusBtn
import kotlinx.android.synthetic.main.item_lv_locations.view.plusBtn


class ItemLocsAdapter(@LayoutRes private val layoutResource: Int, private val docLine: InventoryDocLines, private val clickFunc1: (Loc) -> Unit) : BaseAdapter<Loc>(null, layoutResource) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = items[position]
        var isOnTextChanged = false
        with(holder.itemView){
            loc_name.text = (item.loc_name ?: "")
            loc_qty.text = "${item.qty?.toFormatNumber()}"

            this.tag = item
            this.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val item = it.tag as Loc
                if(item.addQty!! < docLine.invQty!!){
                    if(item.qty!! > docLine.invQty!!) item.addQty = docLine.invQty else item.addQty = item.qty
                }
                clickFunc1(item)
            }
            addQty.setText(docLine.uomSize.toString())
            item.addQty = if(!addQty.text.isNullOrEmpty()) addQty.text!!.toString().toDouble() else 0.0
            addBtn.tag = item
            addBtn.setOnClickListener{
                val data = it.tag as Loc
                clickFunc1(data)
                if(items.count() > 0){
                    addQty.setText(docLine.uomSize.toString())
                    items[position].addQty = docLine.uomSize
                }
            }

            plusBtn.tag = item
            plusBtn.setOnClickListener {
                val data = it.tag as Loc
                if((data.qty!! > data.addQty!!) && (data.addQty!! < docLine.invQty!! )){
                    var qty = data.addQty!! + docLine.uomSize!!
                    if(qty > docLine.invQty!!)
                        qty = docLine.invQty!!

                    items[position].addQty = qty
                    addQty.setText(qty.toString())
                }

            }

            minusBtn.tag = item
            minusBtn.setOnClickListener {
                val data = it.tag as Loc
                if(data.addQty!! > docLine.uomSize!!){
                    var qty = data.addQty!! - docLine.uomSize!!
                    if(qty < docLine.uomSize!!)
                        qty = docLine.uomSize!!
                    items[position].addQty = qty
                    addQty.setText(qty.toString())
                }

            }
            // for add qty
            addQty.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    isOnTextChanged = true
                }

                override fun afterTextChanged(s: Editable?) {
                    if(isOnTextChanged){
                        isOnTextChanged = false
                        try {
                            val qty: Double = s.toString().toDouble()
                            if(qty <= items[position].qty!!){
                                items[position].addQty = qty
                            }

                        }catch (e: NumberFormatException){

                        }
                    }
                }

            })
        }
    }
}

