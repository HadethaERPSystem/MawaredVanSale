package com.mawared.mawaredvansale.controller.marketplace.offers

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.data.db.entities.md.Product_Price_List
import com.mawared.mawaredvansale.data.db.entities.md.UnitConvertion
import com.mawared.mawaredvansale.data.db.entities.sales.OrderItems
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Items
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.OrderRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import java.lang.Exception
import java.util.ArrayList

class OffersViewModel (private val repository: IMDataRepository, private val orderRepository: OrderRepository) : BaseViewModel() {

    var msgListener: IMessageListener? = null
    var ctx: Context? = null
    var customer : Customer? = null
    var price_cat : String = "POS"
    var vocode: String = ""

    //var orders: List<OrderItems> = arrayListOf()
    private var orders: ArrayList<OrderItems> = arrayListOf()
    var _term: MutableLiveData<String?> = MutableLiveData()

    fun loadData(list: MutableList<Product>, term: String, pageCount: Int, loadMore: (List<Product>?, Int) -> Unit){
        try {
            Coroutines.ioThenMain({
                val prdcs = repository.getProductForOffers(App.prefs.savedSalesman?.sm_warehouse_id, price_cat, LocalDate.now(), App.prefs.saveUser!!.org_Id, term, vocode, pageCount)
                if(prdcs != null){
                    list.addAll(prdcs)
                }
            }, {loadMore(list, pageCount)})
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun loadOrders() {
        try {
            Coroutines.io {
                val o = orderRepository.getOrderItems()
                orders.addAll(o)
            }

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun loadUom(prod_Id: Int, success: (List<UnitConvertion>?) -> Unit) {
        Coroutines.ioThenMain({
            var uc: List<UnitConvertion>? = null
            try {
                uc = repository.uom_GetByProduct(prod_Id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@ioThenMain uc
        },
            {
                if (it != null) {
                    success(it)
                }
            }
        )
    }

    fun getLastPrice(prod_Id: Int, PriceCode: String, uomId: Int, success: (Product_Price_List?) -> Unit) {
        Coroutines.ioThenMain({
            var price: Product_Price_List? = null
            try {
                price = repository.product_getLastPrice(prod_Id, PriceCode, uomId, App.prefs.saveUser!!.sl_cr_code!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@ioThenMain price
        },
            {
                success(it)
            }
        )
    }

    fun addOrder(p: Product, success:(() -> Unit) = {}) {
        if (isValid(p)) {
            try {

                val user = App.prefs.saveUser!!
                val strDate = LocalDateTime.now()
                var baseEo : OrderItems? = null
                var ogQty: Double = 0.0
                val isGift = p.pr_isGift
                var oRowIsGift = false
                var isPromo = "N"
                baseEo = if (p.pr_batch_no.isNullOrEmpty()) {
                    orders.find { it.od_prod_Id == p.pr_Id && it.od_wr_Id == p.pr_wr_Id && it.od_uom_Id == p.pr_SUoMEntry}// && it.od_isGift == p.pr_isGift }
                } else {
                    orders.find { it.od_prod_Id == p.pr_Id && it.od_wr_Id == p.pr_wr_Id && it.od_uom_Id == p.pr_SUoMEntry && it.od_batch_no == p.pr_batch_no && it.od_expiry_date == p.pr_expiry_date}// && it.od_isGift == p.pr_isGift }
                }

                var discAmntPcs: Double = baseEo?.od_disc_amnt_pcs ?: p.pr_disc_amnt
                var discAmnt: Double = 0.0

                oRowIsGift = baseEo?.od_isGift ?: false

                var addQty: Double = p.addQty!!

                if (baseEo?.od_pack_qty != null) {
                    addQty += (baseEo.od_pack_qty!!)
                    ogQty = baseEo.od_gift_qty!!
                }

                val pcs = (addQty * p.pr_NumInSale!!)

                var promQty: Double = 0.0
                if (!oRowIsGift && p.prom_qty != null && p.prom_qty != 0.0 && p.pr_NumInSale == 1.0) {
                    var vector: Int = 0
                    vector = (pcs / p.prom_qty!!).toInt()
                    promQty = vector * p.prom_ex_qty!!
                    isPromo = "Y"
                }

                var giftQty : Double = 0.0
                if(isGift){
                    giftQty = p.addQty!! + ogQty
                }else{
                    giftQty = ogQty
                }

                val id: Int = (if (orders.isEmpty() ) 1 else orders.maxOf { x -> x.od_Id } + 1)
                val rowNo: Int =
                    (if (orders.isEmpty()) 1 else orders.maxOf { x -> x.od_rowNo!! } + 1)
                if(p.pr_NumInSale!! == 1.0){
                    addQty += promQty
                }

                val qty = pcs + promQty

                var disValue = 0.0
                var lDisPer: Double = 0.0
                val lineTotal = addQty  * p.pr_unit_price!!

                if(promQty > 0){
                    disValue= p.pr_unit_price!! * promQty
                    lDisPer = ((disValue / lineTotal) * 100)
                }
                else if(giftQty > 0){
                    disValue= p.pr_unit_price!! * giftQty
                    lDisPer = ((disValue / lineTotal) * 100)
                }
                else if (p.pr_dis_value != null && p.pr_dis_value != 0.0 /*&& isGift.value == false*/) {
                    if (p.pr_dis_type!! == "P") {
                        lDisPer = p.pr_dis_value!!
                        disValue = (lDisPer / 100) * lineTotal
                    } else {
                        lDisPer = (p.pr_dis_value!! / p.pr_unit_price!!) * 100
                        disValue = p.pr_dis_value!! * addQty
                    }
                }else if(p.pr_user_discPrcnt != 0.0){
                    lDisPer = p.pr_user_discPrcnt
                    disValue = (lDisPer / 100) * lineTotal
                }else if(p.pr_user_disc_amnt != 0.0){
                    discAmntPcs = p.pr_user_disc_amnt
                }
                else if(baseEo?.od_discount != null && baseEo.od_discount != 0.0){
                    lDisPer = baseEo.od_discount!!
                    disValue = lineTotal * (baseEo.od_discount!! / 100)
                }
                discAmnt = p.pr_user_disc_amnt * (addQty - giftQty)
                var add_dis_value: Double = 0.0
                if(baseEo != null){
                    if(baseEo.od_add_dis_per != null && baseEo.od_add_dis_per != 0.0){
                        add_dis_value = (lineTotal - disValue) * (1- (baseEo.od_add_dis_per!! / 100))
                    }
                }
                val netTotal = (lineTotal - (disValue + discAmnt)) - add_dis_value
                val price_afd = (1 - (lDisPer / 100)) * p.pr_unit_price!!
                val isNew : Boolean = baseEo == null
                if (baseEo == null) {
                    baseEo = OrderItems(
                        id, rowNo, p.pr_Id, p.pr_description_ar, p.pr_SUoMEntry, p.pr_SalUnitMsr, addQty,  p.pr_NumInSale,
                        qty,  giftQty + promQty,  p.pr_unit_price,  price_afd, lineTotal, lDisPer,  disValue, 0.0,0.0, discAmnt, discAmntPcs,
                        netTotal, p.pr_wr_Id, p.pr_wr_name, p.pr_batch_no, p.pr_expiry_date, p.pr_mfg_date, null, isPromo,
                        null, isGift, "$strDate", "${user.id}","$strDate", "${user.id}"
                    )
                } else {
                    baseEo.od_pack_qty = addQty
                    baseEo.od_unit_qty = qty
                    baseEo.od_line_total = lineTotal
                    baseEo.od_disvalue = disValue
                    baseEo.od_add_dis_value = add_dis_value
                    baseEo.od_discount = lDisPer
                    baseEo.od_disc_amnt = discAmnt
                    baseEo.od_net_total = netTotal
                    baseEo.od_gift_qty = giftQty + promQty
                    baseEo.updated_at = "$strDate"
                    baseEo.updated_by = "${user.id}"
                    baseEo.od_isGift = oRowIsGift
                }

                Coroutines.ioThenMain({
                    try {
                        if(isNew) {
                            orderRepository.addOrderItem(baseEo)
                            orders.add(baseEo)
                        }
                        else{
                            orderRepository.updateOrderItem(baseEo)
                        }

                    } catch (e: Exception) {
                        msgListener?.onFailure("Error message when try to add item. Error is ${e.message}")
                        Log.e("Error", "${e.message}")
                    }
                }, {
                    success()
                })
            } catch (e: Exception) {
                msgListener?.onFailure("Error message when try to add item. Error is ${e.message}")
            }
        } // end if
    } // end addOrder fun

    fun isValid(p: Product) : Boolean{
        var msg: String? = ""
        var addQty : Double = (p.addQty ?: 0.0) * (p.pr_NumInSale ?: 1.0)
        val pr_qty: Double = p.pr_qty ?: 0.0
        val addedQty: Double = 0.0
        if (addQty == 0.0) {
            msg += (if (msg!!.isNotEmpty()) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_invalid_qty)
        }
        if(vocode != "PSOrder") {
            val baseEo = if(p.pr_batch_no.isNullOrEmpty())
                orders.find { it.od_prod_Id == p.pr_Id && it.od_wr_Id == p.pr_wr_Id && it.od_uom_Id == p.pr_SUoMEntry }
            else
                orders.find { it.od_prod_Id == p.pr_Id && it.od_wr_Id == p.pr_wr_Id && it.od_uom_Id == p.pr_uom_Id && it.od_batch_no == p.pr_batch_no && it.od_expiry_date == p.pr_expiry_date }

            if(baseEo != null){
                addQty += baseEo.od_unit_qty!!
            }
            if(pr_qty <= 0){
                msg += (if(msg!!.isNotEmpty()) "\n\r" else "")  + ctx!!.resources!!.getString(R.string.msg_error_not_available_product_qty)
            }
            if(addQty > pr_qty){
                msg += (if(msg!!.isNotEmpty()) "\n\r" else "")  + ctx!!.resources!!.getString(R.string.msg_error_not_required_qty_less_product_qty)
            }
            if (p.pr_isGift && baseEo?.od_isPromotion == "Y"){
                msg += (if(msg!!.isNotEmpty()) "\n\r" else "")  + ctx!!.resources!!.getString(R.string.msg_error_gift_and_promo)
            }
        }

        if (p.pr_unit_price == 0.0) {
            msg += (if(msg!!.isNotEmpty()) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_invalid_price)
        }

        if(!msg.isNullOrEmpty()){
            msgListener?.onFailure(msg)
            return false
        }
        return true
    }
}

