package com.mawared.mawaredvansale.controller.adapters

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes
import com.mawared.mawaredvansale.R

import com.mawared.mawaredvansale.controller.base.BaseAdapter
import com.mawared.mawaredvansale.controller.helpers.extension.setup
import com.mawared.mawaredvansale.data.db.entities.inventory.InventoryDocLines
import com.mawared.mawaredvansale.data.db.entities.md.Loc
import kotlinx.android.synthetic.main.item_rv_invoice_item.view.*


class InvoiceItemsAdapter(@LayoutRes private val layoutResource: Int, private val clickFunc1: (InventoryDocLines, Loc, Double, Success:()-> Unit) -> Unit, private val clickFunc2:(InventoryDocLines, Loc, Double, ItemLocsAdapter)-> Unit) : BaseAdapter<InventoryDocLines>(null, layoutResource) {

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        with(holder.itemView){
            val view = this
            val itemSelectedlocAdapter = ItemLocsAdapter(R.layout.item_lv_locations, item){l ->
                clickFunc1(item, l, l.addQty!!){
                    @Suppress("UNCHECKED_CAST")
                    val sAdp = view.rcv_items_selectedLoc.adapter as BaseAdapter<Any>
                    sAdp.setList(item.itemSelectedLoc)
                }
            }

            val itemlocAdapter = ItemLocsAdapter(R.layout.item_lv_locations, item){
                clickFunc2(it.docLine!!, it, it.addQty!!, itemSelectedlocAdapter)
            }

            @Suppress("UNCHECKED_CAST")
            rcv_items_loc.setup(context, itemlocAdapter as BaseAdapter<Any>, false)

            @Suppress("UNCHECKED_CAST")
            rcv_items_selectedLoc.setup(context, itemSelectedlocAdapter as BaseAdapter<Any>, false)

            doc_prod_name.text = (item.prod_name ?: "")
            doc_qty.text = "${item.invQty}"
            doc_pack_qty.text = "${item.qty} ${item.uomName}"
            doc_pack_size.text = "${item.uomSize}"
            if(!item.itemLocations.isNullOrEmpty()){
               val tmp = loadItemLocation(item, item.itemLocations!!)
                if(item.itemLoc == null) item.itemLoc = arrayListOf()
                item.itemLoc!!.addAll(tmp)
                itemlocAdapter.setList(item.itemLoc!!)
            }

            if(item.itemSelectedLoc != null){
                @Suppress("UNCHECKED_CAST")
                val sAdp = view.rcv_items_selectedLoc.adapter as BaseAdapter<Any>
                sAdp.setList(item.itemSelectedLoc)
            }

            this.tag = item
            this.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val tmp = it.tag as InventoryDocLines
                //clickFunc1(tmp)
            }

        }
    }
    private fun loadItemLocation(si: InventoryDocLines, strLocs: String) : ArrayList<Loc> {
        val mLocs : ArrayList<Loc> = arrayListOf()
        val arr = strLocs.split(";")
        arr.forEach {
            val strL = it.split("|")
            val l = Loc(strL[0].toInt(), strL[1], strL[2].toDouble())
            l.docLine = si
            mLocs.add(l)
        }


        return mLocs
    }

}

