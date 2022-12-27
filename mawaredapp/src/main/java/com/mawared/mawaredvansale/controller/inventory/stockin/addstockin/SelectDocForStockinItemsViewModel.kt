package com.mawared.mawaredvansale.controller.inventory.stockin.addstockin

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.adapters.StockInItemLocsAdapter
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.inventory.*
import com.mawared.mawaredvansale.data.db.entities.md.Loc
import com.mawared.mawaredvansale.data.db.entities.md.Voucher
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.stockin.IStockInRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import org.threeten.bp.LocalDateTime

class SelectDocForStockinItemsViewModel (private val stockService: IStockInRepository, private val masterDataRepository: IMDataRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    private val _wr_id: Int = if(App.prefs.savedSalesman?.sm_warehouse_id != null)  App.prefs.savedSalesman!!.sm_warehouse_id!! else 0
    private val user = App.prefs.saveUser!!
    var ctx: Context? = null
    var msgListener: IMessageListener? = null
    var term: String? = ""
    var doc_id: Int = 0
    var baseEo : InventoryDoc? = null
    var docLine : ArrayList<Stockin_Items> = arrayListOf()
    var isRunning: Boolean = false
    var stockType: String = "Sale"

    var voucher: Voucher? = null
    private val _vo_code: MutableLiveData<String> = MutableLiveData()
    val mVoucher: LiveData<Voucher> = Transformations
        .switchMap(_vo_code){
            masterDataRepository.getVoucherByCode(it)
        }

    fun setVoucherCode(vo_code: String){
        if(_vo_code.value == vo_code){
            return
        }
        _vo_code.value = vo_code
    }

    //var out: Stockin_Items ? = null

    fun loadData(list: MutableList<InventoryDocLines>, doc_id: Int, term: String, pageCount: Int, loadMore: (List<InventoryDocLines>?, Int) -> Unit){
        try {
            Coroutines.ioThenMain({
                val tmp = masterDataRepository.doclinesForStockin(doc_id, term, _wr_id, stockType, pageCount)
                if(tmp != null){
                    list.addAll(tmp)
                }
            }, {loadMore(list, pageCount)})
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun addLine(item: InventoryDocLines, loc: Loc, qty: Double, siadap: StockInItemLocsAdapter){
        if(isValidLine(item, loc, qty)){
            val mItem = docLine.find { it.prodId == item.prodId && it.baseEntry == item.docEntry && it.baseLine == item.lineNum && it.locId == loc.loc_Id }
            val rowNo: Int =  (if (docLine.count() == 0) 1 else docLine.maxOf { x -> x.lineNum!! } + 1)
            val strDate = LocalDateTime.now()
            var invQty = qty
            if(mItem?.invQty != null)
                invQty += mItem.invQty!!

            val mQty : Double = invQty / item.uomSize!!
            val line = Stockin_Items(rowNo, 0, item.prodId, item.prod_name, item.uomEntry, item.uomName, mQty, item.uomSize, invQty, item.invQty,
                loc.loc_Id, loc.loc_name, user.id, item.isGift, baseEo!!.docRefno, baseEo!!.vo_Id, item.docEntry, item.lineNum, item.unitCost,
                "$strDate","${user.id}", "$strDate", "${user.id}")
            docLine.add(line)
            if(item.itemSelectedLoc == null) item.itemSelectedLoc = arrayListOf()
            val tmpLoc = item.itemSelectedLoc?.find{ it.loc_Id == loc.loc_Id}
            if(tmpLoc != null){
                tmpLoc.qty = mQty

            }else{
                item.itemSelectedLoc!!.add(Loc(line.locId, line.locName, invQty))
            }
            siadap.setList(item.itemSelectedLoc)
        }
    }

    fun setLoc(items: List<InventoryDocLines>){
        for(i in items){
            val locs = loadItemLocation(i, i.itemLocations!!)
            for(l in locs){
                val line = docLine.find { it.prodId == i.prodId && it.baseEntry == i.docEntry && it.baseLine == i.lineNum && it.locId == l.loc_Id  }
                if(line != null){
                    if(i.itemSelectedLoc == null) i.itemSelectedLoc = arrayListOf()
                    val tmpLoc = i.itemSelectedLoc?.find{ it.loc_Id == l.loc_Id}
                    if(tmpLoc != null){
                        tmpLoc.qty = line.qty

                    }else{
                        i.itemSelectedLoc!!.add(Loc(line.locId, line.locName, line.invQty))
                    }
                }
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

    fun removeLine(item: InventoryDocLines, loc: Loc, qty: Double, Success: () -> Unit){
        try {

            val  tmpLoc = item.itemSelectedLoc!!.find{ it.loc_Id == loc.loc_Id }
            if(tmpLoc != null){
                if(tmpLoc.qty == qty){
                    tmpLoc.qty = 0.0
                    item.itemSelectedLoc!!.remove(tmpLoc)
                }else{
                    tmpLoc.qty = tmpLoc.qty!! - qty
                }
            }
            val mItem = docLine.find { it.prodId == item.prodId && it.baseEntry == item.docEntry && it.baseLine == item.lineNum && it.locId == loc.loc_Id }
            if(mItem != null && tmpLoc?.qty!! == 0.0){
                item.itemSelectedLoc!!.remove(loc)
            }
            if(mItem != null && mItem.invQty == qty){
                docLine.remove(mItem)
            }else if(mItem != null){
                mItem.invQty = mItem.invQty!! - qty
                mItem.qty = mItem.invQty !!/ item.uomSize!!
            }
            Success()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun isValidLine(item: InventoryDocLines, loc: Loc, qty: Double) : Boolean{
        var isSuccessful = true
        var msg: String? = ""
        val tQty = qty

        val mItem = docLine.find { it.prodId == item.prodId && it.baseEntry == item.docEntry && it.baseLine == item.lineNum && it.locId == loc.loc_Id }
        val dl = docLine.find { it.prodId == item.prodId && it.baseEntry == item.docEntry && it.baseLine == item.lineNum }
        val totalQty = dl?.invQty ?: 0.0

        if((totalQty + tQty) > item.invQty!!){
            if(stockType == "Purchase" || stockType == "LocalPurchase") {
                msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_stockin_qty_greater_than_purchase_qty)
            }else if(stockType == "Transfer"){
                msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_stockin_qty_greater_than_transfer_qty)
            }else{
                msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_stockin_qty_greater_than_sale_qty)
            }
        }

//        if(mItem != null){
//            tQty += mItem.invQty!!
//        }
//        if(tQty > loc.qty!!){
//            if(stockType == "Purchase" || stockType == "LocalPurchase") {
//                msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_stockout_qty_greater_than_loc_qty)
//            }
//            else {
//                msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_stockout_qty_greater_than_loc_qty)
//            }
//        }

        if (!msg.isNullOrEmpty()) {
            isSuccessful = false
            msgListener?.onFailure(msg)
        }

        return isSuccessful
    }

    fun onSave(Success:(() -> Unit) = {}, Fail:(() -> Unit) = {}){
        if(isValid()){
            val dtFull = LocalDateTime.now()
            val strDate = LocalDateTime.now()
            val invStats = stockType

            val obj = Stockin(user.cl_Id, user.org_Id, 0, "$dtFull", "${voucher?.vo_prefix}", voucher!!.vo_Id, voucher!!.vo_name, voucher!!.vo_code,
                               baseEo!!.bp_Id, baseEo!!.docEntry, baseEo!!.docRefno, invStats, _wr_id, App.prefs.savedSalesman!!.sm_warehouse_name, false, " اخراج مخزني للفاتورة ${baseEo!!.docRefno}",
                              "$strDate", "${user.id}", "$strDate", "${user.id}"
            )

            obj.docLines.addAll(docLine)
            // call api to save stock-out
            Coroutines.ioThenMain({
                try {
                    val response = stockService.saveOrUpdate(obj)
                    return@ioThenMain response.message
                }catch (e: Exception){
                    return@ioThenMain e.message
                }

            },{
                if(it.isNullOrEmpty()){
                    Success()
                }else{
                    msgListener?.onFailure("Error message when try to save stock-out document. Error is ${it}")
                    Fail()
                }
            })
        }
    }

    fun isValid() : Boolean{
        var isSuccessful = true
        var msg: String? = ""

        if(baseEo == null){
            msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_stockout_select_invoice)
        }
        if(docLine.count() == 0){
            msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_stockout_no_item_selected)
        }
        if (!msg.isNullOrEmpty()) {
            isSuccessful = false
            msgListener?.onFailure(msg)
        }

        return isSuccessful
    }
}