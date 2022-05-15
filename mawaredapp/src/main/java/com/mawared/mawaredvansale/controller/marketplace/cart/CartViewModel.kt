package com.mawared.mawaredvansale.controller.marketplace.cart

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Currency_Rate
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.md.Voucher
import com.mawared.mawaredvansale.data.db.entities.sales.*
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.services.repositories.OrderRepository
import com.mawared.mawaredvansale.services.repositories.invoices.IInvoiceRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.order.IOrderRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import org.threeten.bp.LocalDateTime
import java.lang.Exception

class CartViewModel(private val saleOrderRepository: IOrderRepository, private val saleRepository: IInvoiceRepository, private val repository: IMDataRepository, private val orderRepository: OrderRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    private val user = App.prefs.saveUser!!
    var msgListener: IMessageListener? = null
    var ctx: Context? = null

    var customer : Customer? = null
    var customer_name: MutableLiveData<String> = MutableLiveData()
    var isRunning: Boolean = false
    // google map location GPS
    var location: Location? = null

    // Total to display
    var totalAmount : MutableLiveData<Double> = MutableLiveData()
    var netTotal: MutableLiveData<Double> = MutableLiveData()
    var totalDiscount: MutableLiveData<Double> = MutableLiveData()

    var paidUSD = MutableLiveData<String>()
    var changeUSD= MutableLiveData<String>()
    var paidIQD= MutableLiveData<String>()
    var changeIQD= MutableLiveData<String>()
    var remainAmount = MutableLiveData<Double>()
    var fc_remainAmount = MutableLiveData<Double>()
    var scr_symbol : MutableLiveData<String> = MutableLiveData(App.prefs.saveUser!!.ss_cr_code!!)
    var fcr_symbol : MutableLiveData<String> = MutableLiveData(App.prefs.saveUser!!.sf_cr_code!!)
    var fc_amount: MutableLiveData<Double> = MutableLiveData()

    var price_cat_code = "POS"

    var orders: List<OrderItems> = arrayListOf()
    fun loadOrders(Success:(() -> Unit) = {}){
        try {
               Coroutines.ioThenMain({orders = orderRepository.getOrderItems()},{Success()})

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun refresh(){
        loadOrders(){
            setTotals()
        }
    }
    var voucher: Voucher? = null
    private val _vo_code: MutableLiveData<String> = MutableLiveData()
    val mVoucher: LiveData<Voucher> = Transformations
        .switchMap(_vo_code){
            repository.getVoucherByCode(it)
        }

    var rate : Double = 0.0
    private val _cr_Id: MutableLiveData<Int> = MutableLiveData()
    val currencyRate: LiveData<Currency_Rate> = Transformations
        .switchMap(_cr_Id) {
            repository.getRate(it)
        }

    fun setPriceCategory(){
        price_cat_code = if(customer != null && customer?.cu_price_cat_code != null) customer!!.cu_price_cat_code!! else "POS"
    }

    fun setCurrencyId(cr_id: Int){
        if(_cr_Id.value == cr_id){
            return
        }
        _cr_Id.value = cr_id
    }

    fun setVoucherCode(vo_code: String){
        if(_vo_code.value == vo_code){
            return
        }
        _vo_code.value = vo_code
    }

    fun onSaveInvoice(Success:(() -> Unit) = {}, Fail:((String?) -> Unit) = {}) {
        if (isValidInvoice()) {
            try {
                isRunning = true
                val user = App.prefs.saveUser!!
                val strDate = LocalDateTime.now()
                val totalAmount: Double = orders.sumByDouble { it.od_line_total!! }
                val totalDiscount: Double = orders.sumByDouble { if(it.od_disvalue == null) 0.0 else it.od_disvalue!! }
                val netAmount: Double = orders.sumByDouble { it.od_net_total!! }
                val dtFull = LocalDateTime.now()
                val doc_num =  0
                val cu_Id = customer?.cu_ref_Id
                val price_cat_Id = customer?.cu_price_cat_Id
                // paid
                val amountUSD = if(paidUSD.value == null) 0.0 else paidUSD.value!!.toDouble()
                val change_USD = if(changeUSD.value == null) 0.0 else changeUSD.value!!.toDouble()
                val amountIQD = if(paidIQD.value == null) 0.0 else paidIQD.value!!.toDouble()
                val change_IQD = if(changeIQD.value == null) 0.0 else changeIQD.value!!.toDouble()
                val baseEo = Sale(
                    doc_num, "${dtFull}", "${voucher?.vo_prefix}","", user.cl_Id, user.org_Id, voucher!!.vo_Id,  cu_Id,
                    _sm_id, null, totalAmount, totalDiscount, netAmount, user.ss_cr_Id, user.sf_cr_Id, rate,false,
                    location?.latitude, location?.longitude, price_cat_Id, amountUSD, change_USD, amountIQD, change_IQD,
                    "$strDate", "${user.id}", "$strDate", "${user.id}"
                )
                baseEo.sl_price_cat_code = price_cat_code
                val tmpInvoiceItems = arrayListOf<Sale_Items>()
               for (o in orders){
                   val item = Sale_Items(0, o.od_rowNo, null, o.od_prod_Id,
                       o.od_uom_Id, o.od_pack_qty, o.od_pack_size, o.od_unit_qty, o.od_gift_qty,
                       o.od_unit_price, o.od_price_afd, o.od_line_total, o.od_discount, o.od_disvalue, o.od_net_total, null, null, null,
                       o.od_wr_Id, o.od_batch_no, o.od_expiry_date,  o.od_mfg_date,
                       false,o.created_at,o.created_by, o.updated_at, o.updated_by
                   )
                   tmpInvoiceItems.add(item)
               }
                baseEo.items.addAll(tmpInvoiceItems)

                Coroutines.ioThenMain({
                    var response : ResponseSingle<Sale>? = null
                    try {
                         response = saleRepository.SaveOrUpdate(baseEo)
                    }catch (e: Exception){
                    }
                    return@ioThenMain response
                },
                    {
                        if(it != null && it.isSuccessful){
                            Success()
                        }else{
                            Fail("Error message when try to save sale invoice. Error is ${it?.message}")
                        }

                    })
            }catch (e: Exception){
                Fail("${ctx!!.resources!!.getString(R.string.msg_exception)} Exception is ${e.message}")
            }
        }else{
            Fail(null)
        }
    }

    fun setTotals(){
        totalAmount.postValue(orders.sumByDouble{ it.od_line_total ?: 0.0 } )
        totalDiscount.postValue(orders.sumByDouble { it.od_disvalue  ?: 0.0 })
        val net = orders.sumByDouble { it.od_net_total ?: 0.0 }
        netTotal.postValue(net)
        if(rate != 0.0){
            if(user.cr_Id == user.ss_cr_Id)
                fc_amount.postValue(net * rate)
            else
                fc_amount.postValue(net / rate)

        }
    }

    fun updateRemain(){
        val amountUSD = if(paidUSD.value.isNullOrEmpty()) 0.0 else paidUSD.value!!.toDouble()
        val change_USD = if(changeUSD.value.isNullOrEmpty()) 0.0 else changeUSD.value!!.toDouble()
        val amountIQD = if(paidIQD.value.isNullOrEmpty()) 0.0 else paidIQD.value!!.toDouble()
        val change_IQD = if(changeIQD.value.isNullOrEmpty()) 0.0 else changeIQD.value!!.toDouble()
        val netAmount: Double = orders.sumByDouble { it.od_net_total!! }


        var paidAmount = 0.0
        if (user.ss_cr_code == "IQD") {
            paidAmount = ((amountUSD * rate) + amountIQD) - ((change_USD * rate) + change_IQD)
        } else {
            paidAmount = (amountUSD + (amountIQD / rate)) - (change_USD + (change_IQD / rate))
        }
        remainAmount.value = (netAmount - paidAmount)
        if(rate != 0.0){
            if(user.cr_Id == user.ss_cr_Id)
                fc_remainAmount.postValue( (netAmount - paidAmount) * rate)
            else
                fc_remainAmount.postValue( (netAmount - paidAmount) / rate)
        }
    }

    private fun isValidInvoice(): Boolean {
        var isSuccess = true
        var msg: String? = ""

        if (customer == null) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_customer)
        }

        if (orders.count() == 0) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_items)
        }

        if (App.prefs.saveUser == null) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_currency)
        }
        val amountUSD = if(paidUSD.value == null) 0.0 else paidUSD.value!!.toDouble()
        val change_USD = if(changeUSD.value == null) 0.0 else changeUSD.value!!.toDouble()
        val amountIQD = if(paidIQD.value == null) 0.0 else paidIQD.value!!.toDouble()
        val change_IQD = if(changeIQD.value == null) 0.0 else changeIQD.value!!.toDouble()
        val netAmount: Double = orders.sumByDouble { it.od_net_total!! }

        if(netAmount > 0){
            if(customer != null){
                if(customer!!.payCode!!.contains("CREDIT")) {
                    if (customer?.cu_credit_limit != null && customer?.cu_credit_limit != 0.0) {

                        val bal = customer!!.cu_balance ?: 0.0
                        val user = App.prefs.saveUser!!
                        var amount = 0.0
                        var paidAmount = 0.0
                        if (user.ss_cr_code == "IQD") {
                            paidAmount = ((amountUSD * rate) + amountIQD) - ((change_USD * rate) + change_IQD)
                        } else {
                            paidAmount = (amountUSD + (amountIQD / rate)) - (change_USD + (change_IQD / rate))
                        }
                        amount = (bal) + netAmount - paidAmount
                        if(amount < -1){
                            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(
                                R.string.msg_error_remain_amount)
                        }
                        if (customer!!.cu_credit_limit!! < amount) {
                            msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(
                                R.string.msg_error_credit_limit)
                        }
                    }

                    if (customer?.cu_credit_days != null && customer?.cu_credit_days != 0) {
                        if (customer!!.cu_credit_age > customer!!.cu_credit_days!!) {
                            msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(
                                R.string.msg_error_credit_limit)
                        }
                    }
                }
                else{
                    if(amountUSD == 0.0 && amountIQD == 0.0){
                        msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_not_paid_amount)
                    }
                    val user = App.prefs.saveUser!!
                    var amount = 0.0
                    var paidAmount = 0.0
                    if (user.ss_cr_code == "IQD") {
                        paidAmount = ((amountUSD * rate) + amountIQD) - ((change_USD * rate) + change_IQD)
                    } else {
                        paidAmount = (amountUSD + (amountIQD / rate)) - (change_USD + (change_IQD / rate))
                    }
                    amount = netAmount - paidAmount
                    if(amount >= 1 || amount <= -1){
                        msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_not_paid_amount)
                    }

                }
            }
        }

        if (!msg.isNullOrEmpty()) {
            isSuccess = false
            msgListener?.onFailure(msg)
        }
        return isSuccess
    }

    // Save order
    fun onSaveOrder(Success:() -> Unit, Fail: (String?) -> Unit){
        if(isValidOrder()){
            try {
                isRunning = true
                val user = App.prefs.saveUser!!
                val totalAmount: Double = orders.sumByDouble { it.od_line_total!! }
                val netAmount: Double = orders.sumByDouble { it.od_net_total!! }
                val totalDiscount = orders.sumByDouble { it.od_disvalue!! }
                val strDate= LocalDateTime.now()
                val dtFull = LocalDateTime.now()
                val doc_num =  0
                val cu_Id = customer?.cu_ref_Id
                val price_cat_Id = customer?.cu_price_cat_Id
                val baseEo = Sale_Order( user.cl_Id, user.org_Id, doc_num, "${dtFull}",
                    "", mVoucher.value!!.vo_prefix, mVoucher.value!!.vo_Id,
                    _sm_id, cu_Id, null, null, totalAmount, totalDiscount, netAmount, user.ss_cr_Id, rate,
                    false, location?.latitude, location?.longitude, price_cat_Id, "$strDate",
                    "${user.id}", "$strDate", "${user.id}"
                )
                baseEo.so_price_cat_code =  price_cat_code

                baseEo.items = arrayListOf()
                for (o in orders){
                    val itemEo = Sale_Order_Items(o.od_rowNo, 0, o.od_prod_Id, o.od_uom_Id, o.od_unit_qty, o.od_pack_size, o.od_pack_qty, o.od_gift_qty, o.od_unit_price, o.od_price_afd,
                        o.od_line_total, o.od_discount, o.od_disvalue, o.od_net_total, o.od_wr_Id,  o.od_batch_no,  o.od_expiry_date,
                        o.od_mfg_date, null, null, null, false,o.created_at, o.created_by, o.updated_at, o.updated_by)
                    itemEo.sod_prod_name = o.od_prod_name
                    itemEo.sod_barcode = ""
                    itemEo.sod_partno = ""
                    itemEo.sod_pr_is_batch = ""

                    baseEo.items.add(itemEo)
                }

                Coroutines.ioThenMain({
                    var response : ResponseSingle<Sale_Order>? = null
                    try {
                        response = saleOrderRepository.SaveOrUpdate(baseEo)
                    }catch (e: Exception){
                    }
                    return@ioThenMain response
                },
                    {
                        if(it != null && it.isSuccessful){
                            Success()
                        }else{
                            Fail("Error message when try to save sale invoice. Error is ${it?.message}")
                        }

                    })

            }catch (e: Exception){
                Fail("${ctx!!.resources!!.getString(R.string.msg_exception)} Exception is ${e.message}")
            }
        }
    }

    private fun isValidOrder(): Boolean{
        var isSuccessful = true
        var msg: String? = ""

        if (customer == null) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_customer)
        }

        if (orders.count() == 0) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_items)
        }

        if (App.prefs.saveUser == null) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_currency)
        }

        if (!msg.isNullOrEmpty()) {
            isSuccessful = false
            msgListener?.onFailure(msg)
        }
        return isSuccessful
    }

    fun delete(item: OrderItems, Success:() -> Unit, Fail:() -> Unit){
        try {
            Coroutines.io {
                orderRepository.deleteItem(item)
                Success()
            }
        }catch (e: Exception){
            e.printStackTrace()
            Fail()
        }
    }

    fun deleteAll(Success:() -> Unit = {}, Fail:() -> Unit = {}){
        try {
            Coroutines.ioThenMain({
                orderRepository.deleteAllItems()
            },
            {
                Success()
            })
        }catch (e: Exception){
            e.printStackTrace()
            Fail()
        }
    }
}