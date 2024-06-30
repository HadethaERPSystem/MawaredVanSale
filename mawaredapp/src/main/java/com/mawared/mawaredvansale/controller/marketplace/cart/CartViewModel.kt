package com.mawared.mawaredvansale.controller.marketplace.cart

import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.controller.common.GenerateTicket
import com.mawared.mawaredvansale.controller.common.SunmiTicket
import com.mawared.mawaredvansale.controller.common.printing.*
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
import com.mawared.mawaredvansale.utilities.ImageLoader
import com.mawared.mawaredvansale.utilities.URL_LOGO
import com.mawared.mawaredvansale.utils.SunmiPrintHelper
import org.threeten.bp.LocalDateTime
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.text.DecimalFormat
import java.util.*

class CartViewModel(private val saleOrderRepository: IOrderRepository, private val saleRepository: IInvoiceRepository, private val repository: IMDataRepository, private val orderRepository: OrderRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    private val user = App.prefs.saveUser!!
    val mLimitDiscPrcnt: Double = App.prefs.saveUser!!.dDiscPrcnt

    var msgListener: IMessageListener? = null
    var ctx: Context? = null
    var activity: Activity? = null
    var customer : Customer? = null
    var customer_name: MutableLiveData<String> = MutableLiveData()
    var ccustomer_name: MutableLiveData<String> = MutableLiveData()
    var discPrcnt: Double? = 0.0
    var tmpDiscPrcnt: Double? = 0.0
    var notes : MutableLiveData<String> = MutableLiveData()
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
            orders = arrayListOf()
            Coroutines.ioThenMain({
                orders = orderRepository.getOrderItems()

            },
             { Success() })

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    var ageDebit: Customer ?= null
    fun loadAgeDebit(cu_Id: Int){
        try {
            Coroutines.ioThenMain({ageDebit = repository.customer_getAgeDebit(cu_Id)},{})

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

    fun onSaveInvoice(Success:((Sale?) -> Unit) = {}, Fail:((String?) -> Unit) = {}) {
        if (isValidInvoice()) {
            try {

                isRunning = true
                val user = App.prefs.saveUser!!
                val strDate = LocalDateTime.now()
                val totalAmount: Double = orders.sumByDouble { it.od_line_total!! }
                val totalDiscount: Double = orders.sumByDouble { (if(it.od_disvalue == null) 0.0 else it.od_disvalue!!) + (if(it.od_add_dis_value == null) 0.0 else it.od_add_dis_value!!)  + (if(it.od_disc_amnt == null) 0.0 else it.od_disc_amnt!!) }
                val netAmount: Double = orders.sumByDouble { it.od_net_total!! }
                //val tmpDiscPrcnt : Double = if(discPrcnt.value != null) discPrcnt.value!! else 0.0
                //val net1 = netAmount * (1- (tmpDiscPrcnt/100))
                //val tdisc = totalDiscount + (netAmount - net1)
                val dtFull = LocalDateTime.now()
                val doc_num =  0
                val cu_Id = customer?.cu_ref_Id
                val price_cat_Id = customer?.cu_price_cat_Id
                // paid
                val amountUSD = if(paidUSD.value.isNullOrEmpty()) 0.0 else paidUSD.value!!.toDouble()
                val change_USD = if(changeUSD.value.isNullOrEmpty()) 0.0 else changeUSD.value!!.toDouble()
                val amountIQD = if(paidIQD.value.isNullOrEmpty()) 0.0 else paidIQD.value!!.toDouble()
                val change_IQD = if(changeIQD.value.isNullOrEmpty()) 0.0 else changeIQD.value!!.toDouble()
                //val cuId = if(App.prefs.saveUser!!.sl_cr_code!! == App.prefs.saveUser!!.ss_cr_code!!) App.prefs.saveUser!!.sf_cr_Id!! else App.prefs.saveUser!!.ss_cr_Id!!
                val baseEo = Sale(
                    doc_num, "${dtFull}", "${voucher?.vo_prefix}","", user.cl_Id, user.org_Id, voucher!!.vo_Id,  cu_Id,
                    ccustomer_name.value, _sm_id, null, totalAmount, totalDiscount, netAmount,  (discPrcnt ?: 0.0), user.ss_cr_Id, user.sf_cr_Id, rate,false,
                    location?.latitude, location?.longitude, price_cat_Id, amountUSD, change_USD, amountIQD, change_IQD, notes.value,
                    "$strDate", "${user.id}", "$strDate", "${user.id}"
                )
                baseEo.sl_price_cat_code = price_cat_code
                val tmpInvoiceItems = arrayListOf<Sale_Items>()

               for (o in orders){
                   val item = Sale_Items(0, o.od_rowNo, null, o.od_prod_Id,
                       o.od_uom_Id, o.od_pack_qty, o.od_pack_size, o.od_unit_qty, o.od_gift_qty, o.od_unit_price, o.od_price_afd, o.od_line_total,
                       o.od_discount, o.od_disvalue, o.od_add_dis_per, o.od_add_dis_value, o.od_disc_amnt, o.od_net_total, null, null, null,
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
                            Success(it.data)
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

    fun recalculateTotal(Success:(() -> Unit) = {}, Fail:((Double?) -> Unit) = {}){
        try {
            if(discPrcnt == null) discPrcnt = 0.0

            if(discPrcnt != null && discPrcnt != 0.0){
                if(discPrcnt!! > mLimitDiscPrcnt){
                    val msg1 = ctx!!.resources!!.getString(R.string.msg_error_disc_limit)
                    msgListener?.onFailure(String.format(msg1,  mLimitDiscPrcnt.toString()))
                    Fail(tmpDiscPrcnt)
                    discPrcnt = tmpDiscPrcnt
                    return
                }
            }

            for (o in orders){
                o.od_add_dis_per = discPrcnt!!
                o.od_add_dis_value = (o.od_line_total!! - o.od_disvalue!!) * (discPrcnt!! / 100)
                o.od_net_total = (o.od_line_total!! - o.od_disvalue!!) * (1- (discPrcnt!!/100))
            }
            tmpDiscPrcnt = discPrcnt
            Success()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun setTotals(){
        totalAmount.postValue(orders.sumByDouble{ it.od_line_total ?: 0.0 } )
        totalDiscount.postValue(orders.sumByDouble { (it.od_disvalue  ?: 0.0) + (it.od_add_dis_value ?: 0.0) + (it.od_disc_amnt ?: 0.0) })
        val net = orders.sumByDouble { it.od_net_total ?: 0.0 }
        netTotal.postValue(net)
        if(rate != 0.0){
            if(user.isDirectRate == "Y")
                fc_amount.postValue(net / rate)
            else
                fc_amount.postValue(net * rate)

        }
    }

    fun getPaidAmount(amountUSD: Double, amountIQD: Double, change_USD: Double, change_IQD: Double) : Double{
        var paidAmount = 0.0
        if(App.prefs.saveUser!!.ss_cr_code == "$"){
            if (user.isDirectRate == "Y") {
                paidAmount = (amountUSD + (amountIQD / rate)) - (change_USD + (change_IQD / rate))
            }else{
                paidAmount = (amountUSD + (amountIQD * rate)) - (change_USD + (change_IQD * rate))
            }
        }else{
            if (user.isDirectRate == "Y") {
                paidAmount = ((amountUSD * rate) + amountIQD) - ((change_USD * rate) + change_IQD)
            }else{
                paidAmount = ((amountUSD / rate) + amountIQD) - ((change_USD / rate) + change_IQD)
            }
        }
        return paidAmount
    }

    fun updateRemain(){
        val amountUSD = if(paidUSD.value.isNullOrEmpty()) 0.0 else paidUSD.value!!.toDouble()
        val change_USD = if(changeUSD.value.isNullOrEmpty()) 0.0 else changeUSD.value!!.toDouble()
        val amountIQD = if(paidIQD.value.isNullOrEmpty()) 0.0 else paidIQD.value!!.toDouble()
        val change_IQD = if(changeIQD.value.isNullOrEmpty()) 0.0 else changeIQD.value!!.toDouble()
        val netAmount: Double = orders.sumByDouble { it.od_net_total!! }

        val paidAmount = getPaidAmount(amountUSD, amountIQD, change_USD, change_IQD)

        remainAmount.value = (netAmount - paidAmount)
        if(rate != 0.0){
            if(App.prefs.saveUser!!.ss_cr_code == "$"){
                if(user.isDirectRate == "Y")
                    fc_remainAmount.postValue( (netAmount - paidAmount) * rate)
                else
                    fc_remainAmount.postValue( (netAmount - paidAmount) / rate)
            }else{
                if(user.isDirectRate == "Y")
                    fc_remainAmount.postValue( (netAmount - paidAmount) / rate)
                else
                    fc_remainAmount.postValue( (netAmount - paidAmount) * rate)
            }

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
        val amountUSD = if(paidUSD.value.isNullOrEmpty()) 0.0 else paidUSD.value!!.toDouble()
        val change_USD = if(changeUSD.value.isNullOrEmpty()) 0.0 else changeUSD.value!!.toDouble()
        val amountIQD = if(paidIQD.value.isNullOrEmpty()) 0.0 else paidIQD.value!!.toDouble()
        val change_IQD = if(changeIQD.value.isNullOrEmpty()) 0.0 else changeIQD.value!!.toDouble()
        val netAmount: Double = orders.sumByDouble { it.od_net_total!! }

        val paidAmount = getPaidAmount(amountUSD, amountIQD, change_USD, change_IQD)

        if(discPrcnt != null && discPrcnt != 0.0){
            if(discPrcnt!! > mLimitDiscPrcnt){
                val str = ctx!!.resources!!.getString(R.string.msg_error_disc_limit)
                msg += (if (msg!!.length > 0) "\n\r" else "")  + String.format(str,  mLimitDiscPrcnt)
            }
        }

        if(netAmount > 0){
            if(customer != null){
                if(customer!!.payCode!!.contains("CREDIT")) {
                    if (customer?.cu_credit_limit != null && customer?.cu_credit_limit != 0.0) {

                        val bal = customer!!.cu_balance ?: 0.0
                        //val user = App.prefs.saveUser!!

                        val amount = (bal) + netAmount - paidAmount
//                        if(amount >= user.dropAmnt!! || amount <= -user.dropAmnt!!){
//                            if(user.dropAmnt != 0.0){
//                                msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_remain_amount)
//                            }else if(amount != 0.0){
//                                msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_not_paid_amount)
//                            }
//                        }
                        if (customer!!.cu_credit_limit!! < amount) {
                            msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(
                                R.string.msg_error_credit_limit)
                        }
                    }
                    var cu_AgeDebit : Int = 0
                    if(ageDebit != null){
                        cu_AgeDebit = ageDebit!!.cu_DebitAge ?: 0
                    }
                    if (customer?.cu_credit_days != null && customer?.cu_credit_days != 0) {
                        if (cu_AgeDebit > customer!!.cu_credit_days!!) {
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

                    val amount = netAmount - paidAmount
                    if(amount >= user.dropAmnt!! || amount <= -user.dropAmnt!!){
                        if(user.dropAmnt != 0.0){
                            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_not_paid_amount)
                        }else if(amount != 0.0){
                            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_not_paid_amount)
                        }
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
    fun onSaveOrder(Success:(Sale_Order?) -> Unit, Fail: (String?) -> Unit){
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
                    _sm_id, cu_Id, ccustomer_name.value, null, totalAmount, totalDiscount, netAmount, (discPrcnt ?: 0.0), user.ss_cr_Id, rate,
                    false, location?.latitude, location?.longitude, price_cat_Id, notes.value, "$strDate",
                    "${user.id}", "$strDate", "${user.id}"
                )
                baseEo.so_price_cat_code =  price_cat_code

                baseEo.items = arrayListOf()
                for (o in orders){
                    val itemEo = Sale_Order_Items(o.od_rowNo, 0, o.od_prod_Id, o.od_uom_Id, o.od_unit_qty, o.od_pack_size, o.od_pack_qty, o.od_gift_qty, o.od_unit_price, o.od_price_afd,
                        o.od_line_total, o.od_discount, o.od_disvalue, o.od_add_dis_per, o.od_add_dis_value, o.od_disc_amnt, o.od_net_total, o.od_wr_Id,  o.od_batch_no,  o.od_expiry_date,
                        o.od_mfg_date, null, null, null, o.od_isGift,o.created_at, o.created_by, o.updated_at, o.updated_by)
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
                            Success(it.data)
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

        if (customer?.cu_credit_limit != null && customer?.cu_credit_limit!! > 0){
            val netAmount: Double = orders.sumByDouble { it.od_net_total!! }

            if (customer!!.cu_credit_limit!! < netAmount) {
                msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_credit_limit)
            }
        }

        if(discPrcnt != null && discPrcnt != 0.0){
            if(discPrcnt!! > mLimitDiscPrcnt){
                val str = ctx!!.resources!!.getString(R.string.msg_error_disc_limit)
                msg += (if (msg!!.length > 0) "\n\r" else "")  + String.format(str,  mLimitDiscPrcnt)
            }
        }

        var cu_AgeDebit : Int = 0
        if(ageDebit != null){
            cu_AgeDebit = ageDebit!!.cu_DebitAge ?: 0
        }
        if (customer?.cu_credit_days != null && customer?.cu_credit_days != 0) {
            if (cu_AgeDebit > customer!!.cu_credit_days!!) {
                msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(
                    R.string.msg_error_credit_limit)
            }
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

    // Print
    fun doPrint(entityEo: Sale){
        if(App.prefs.printing_type == "R") {
            try {
                val lang = Locale.getDefault().toString().toLowerCase()
                entityEo.sl_salesman_phone = App.prefs.savedSalesman?.sm_phone_no ?: ""

                var bmp : Bitmap? = null
                ImageLoader().LoadImageFromUrl(URL_LOGO + "co_black_logo.png") {
                    bmp = it
                    val tickets = GenerateTicket(ctx!!, lang).createSunmiTicket(
                        entityEo,
                        bmp,
                        "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION",
                        null,
                        null
                    )
                    SunmiPrintHelper.getInstance().printReceipt(ctx!!, tickets)
                    msgListener!!.onSuccess("Print Successfully")
                }
            }catch (e: Exception){
                msgListener!!.onFailure("Error Exception ${e.message}")
                e.printStackTrace()
            }

        }else {
            //val lang = Locale.getDefault().toString().toLowerCase()
            val config = ctx!!.resources.configuration
            val isRTL = config.layoutDirection != View.LAYOUT_DIRECTION_LTR
            var bmp: Bitmap? =
                null // BitmapFactory.decodeResource(ctx!!.resources, R.drawable.ic_logo_black)

            val mngr: AssetManager = ctx!!.assets
            var `is`: InputStream? = null
            try {
                //`is` = mngr.open("images/co_logo.bmp")
                //bmp = BitmapFactory.decodeStream(`is`)
                //URL_LOGO + "co_black_logo.png"
                //`is` = mngr.open("images/co_logo.bmp")
                //bmp = BitmapFactory.decodeStream(`is`)
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
            val fontNameEn = "assets/fonts/arial.ttf"
            val fontNameAr = "assets/fonts/arial.ttf"// "assets/fonts/droid_kufi_regular.ttf"
            try {

                val imgLogo = RepLogo(bmp, 10F, 800F)
                val header: ArrayList<HeaderFooterRow> = arrayListOf()
                var tbl: HashMap<Int, TCell> = hashMapOf()
                val rws: ArrayList<CTable> = arrayListOf()
                val phones = if (entityEo.sl_org_phone != null) entityEo.sl_org_phone!!.replace(
                    "|",
                    "\n\r"
                ) else ""

                header.add(
                    HeaderFooterRow(
                        0,
                        null,
                        App.prefs.saveUser!!.client_name,
                        14F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameEn
                    )
                )
                header.add(
                    HeaderFooterRow(
                        1,
                        null,
                        "${entityEo.sl_org_name}",
                        14F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameEn
                    )
                )
                header.add(
                    HeaderFooterRow(
                        2,
                        null,
                        phones,
                        11F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameEn
                    )
                )
                //header.add(HeaderFooterRow(3, null, "Asia: 0770-6502228", 20F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))
                header.add(
                    HeaderFooterRow(
                        3,
                        null,
                        "",
                        14F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameAr
                    )
                )
                header.add(
                    HeaderFooterRow(
                        4,
                        null,
                        "",
                        14F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameEn
                    )
                )
                header.add(
                    HeaderFooterRow(
                        5,
                        null,
                        "",
                        14F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameEn
                    )
                )

                tbl.put(0, TCell("", 9F, false, 2f, "", Element.ALIGN_CENTER, 0))
                tbl.put(
                    1,
                    TCell(
                        ctx!!.resources!!.getString(R.string.rpt_list_name),
                        9f,
                        false,
                        15F,
                        "",
                        Element.ALIGN_RIGHT,
                        0
                    )
                )
                tbl.put(
                    2,
                    TCell(
                        entityEo.sl_vo_name!!,
                        9F,
                        false,
                        30F,
                        "",
                        Element.ALIGN_RIGHT,
                        0
                    )
                )

                tbl.put(
                    3,
                    TCell(
                        ctx!!.resources!!.getString(R.string.rpt_invoice_no),
                        9F,
                        false,
                        15F,
                        "",
                        Element.ALIGN_RIGHT,
                        0
                    )
                )
                tbl.put(
                    4,
                    TCell(
                        entityEo.sl_refNo!!,
                        9F,
                        false,
                        30F,
                        "",
                        Element.ALIGN_RIGHT,
                        0
                    )
                )

                tbl.put(
                    5,
                    TCell(
                        ctx!!.resources!!.getString(R.string.rpt_invoice_date),
                        9F,
                        false,
                        10F,
                        "",
                        Element.ALIGN_RIGHT,
                        0,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    6,
                    TCell(
                        returnDateString(entityEo.sl_doc_date!!),
                        9F,
                        false,
                        25F,
                        "",
                        Element.ALIGN_RIGHT,
                        0,
                        fontName = fontNameAr
                    )
                )

                tbl.put(7, TCell("", 9F, false, 15F, "", Element.ALIGN_CENTER, 0))
                tbl.put(8, TCell("", 9F, false, 10F, "", Element.ALIGN_CENTER, 0))
                tbl.put(9, TCell("", 9F, false, 2F, "", Element.ALIGN_CENTER, 0))
                rws.add(CTable(tbl))
                tbl = hashMapOf()

                tbl.put(0, TCell("", 9F, false, 12F, "", Element.ALIGN_CENTER, 0))
                tbl.put(
                    1,
                    TCell(
                        ctx!!.resources!!.getString(R.string.rpt_customer),
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        0,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    2,
                    TCell(
                        entityEo.sl_customer_name!!,
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        0,
                        fontName = fontNameAr
                    )
                )

                tbl.put(
                    3,
                    TCell(
                        ctx!!.resources!!.getString(R.string.rpt_contact_name),
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        0,
                        fontNameAr
                    )
                )
                tbl.put(
                    4,
                    TCell(
                        "${entityEo.sl_contact_name}",
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        0,
                        fontNameAr
                    )
                )

                tbl.put(
                    5,
                    TCell(
                        ctx!!.resources!!.getString(R.string.rpt_phone),
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        0
                    )
                )
                tbl.put(
                    6,
                    TCell(
                        if (entityEo.sl_customer_phone == null) "" else entityEo.sl_customer_phone!!,
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        0
                    )
                )

                tbl.put(
                    7,
                    TCell(
                        ctx!!.resources!!.getString(R.string.rpt_cr_name),
                        9F,
                        false,
                        18F,
                        "",
                        Element.ALIGN_RIGHT,
                        0
                    )
                )
                tbl.put(
                    8,
                    TCell(
                        if (entityEo.sl_cr_name == null) "" else entityEo.sl_cr_name!!,
                        9F,
                        false,
                        18F,
                        "",
                        Element.ALIGN_RIGHT,
                        0
                    )
                )

                tbl.put(9, TCell("", 9F, false, 12F, "", Element.ALIGN_CENTER, 0))
                rws.add(CTable(tbl))

                val cw: ArrayList<Int> = arrayListOf(5, 15, 25, 10, 30, 25, 15, 15, 10, 5)
                header.add(HeaderFooterRow(8, rws, null, cellsWidth = cw))

                val footer: ArrayList<HeaderFooterRow> = arrayListOf()
                footer.add(
                    HeaderFooterRow(
                        0,
                        null,
                        "موارد",
                        fontSize = 9F,
                        align = Element.ALIGN_LEFT,
                        fontName = fontNameAr
                    )
                )
                footer.add(
                    HeaderFooterRow(
                        1,
                        null,
                        "الشركة الحديثة للبرامجيات الاتمتة المحدودة",
                        fontSize = 9F,
                        align = Element.ALIGN_LEFT,
                        fontName = fontNameAr
                    )
                )
                footer.add(
                    HeaderFooterRow(
                        2,
                        null,
                        ctx!!.resources!!.getString(R.string.rpt_user_name) + ": ${App.prefs.saveUser!!.name}",
                        fontSize = 9F,
                        align = Element.ALIGN_LEFT,
                        fontName = fontNameAr
                    )
                )
                val rowHeader: HashMap<Int, RowHeader> = hashMapOf()
                rowHeader.put(0, RowHeader("#", 9.0F, false, 4, "", 0, 0F))
                rowHeader.put(
                    1,
                    RowHeader(
                        ctx!!.resources!!.getString(R.string.rpt_barcode),
                        9.0F,
                        false,
                        15,
                        "",
                        0,
                        0F
                    )
                )
                rowHeader.put(
                    2,
                    RowHeader(
                        ctx!!.resources!!.getString(R.string.rpt_prod_name),
                        9.0F,
                        false,
                        30,
                        "",
                        0,
                        0F
                    )
                )
                rowHeader.put(
                    3,
                    RowHeader(
                        ctx!!.resources!!.getString(R.string.rpt_qty),
                        9.0F,
                        false,
                        5,
                        "",
                        0,
                        0F
                    )
                )
                rowHeader.put(
                    4,
                    RowHeader(
                        ctx!!.resources!!.getString(R.string.unit_name),
                        9.0F,
                        false,
                        5,
                        "",
                        0,
                        0F
                    )
                )
                rowHeader.put(
                    5,
                    RowHeader(
                        ctx!!.resources!!.getString(R.string.rpt_gift),
                        9.0F,
                        false,
                        5,
                        "",
                        0,
                        0F
                    )
                )
                rowHeader.put(
                    6,
                    RowHeader(
                        ctx!!.resources!!.getString(R.string.rpt_unit_price),
                        9.0F,
                        false,
                        11,
                        "",
                        0,
                        0F
                    )
                )
                rowHeader.put(
                    7,
                    RowHeader(
                        ctx!!.resources!!.getString(R.string.rpt_dis_value),
                        9.0F,
                        false,
                        7,
                        "",
                        0,
                        0F
                    )
                )
                rowHeader.put(
                    8,
                    RowHeader(
                        ctx!!.resources!!.getString(R.string.rpt_net_total),
                        9.0F,
                        false,
                        11,
                        "",
                        0,
                        0F
                    )
                )
                rowHeader.put(
                    9,
                    RowHeader(
                        ctx!!.resources!!.getString(R.string.rpt_notes),
                        9.0F,
                        false,
                        13,
                        "Total",
                        0,
                        0F
                    )
                )

                // Summary part
                val df1 = DecimalFormat("#,###")
                val df2 = DecimalFormat("#,###,###.#")
                val summary: ArrayList<HeaderFooterRow> = arrayListOf()
                tbl = hashMapOf()
                var srows: ArrayList<CTable> = arrayListOf()
                val tQty = entityEo.items.sumByDouble { it.sld_pack_qty!! }
                tbl.put(
                    0,
                    TCell(
                        ctx!!.resources!!.getString(R.string.rpt_total_qty),
                        9F,
                        false,
                        25F,
                        "",
                        Element.ALIGN_RIGHT,
                        1,
                        fontName = fontNameAr
                    )
                )
                tbl.put(1, TCell("${df1.format(tQty)}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1))
                srows.add(CTable(tbl))

                tbl = hashMapOf()
                val tweight =
                    entityEo.items.sumByDouble { if (it.sld_total_weight == null) 0.00 else it.sld_total_weight!! }
                tbl.put(
                    0,
                    TCell(
                        ctx!!.resources!!.getString(R.string.rpt_total_weight),
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        1,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    1,
                    TCell("${df2.format(tweight)}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1)
                )
                srows.add(CTable(tbl))
                // row 2
                tbl = hashMapOf()
                tbl.put(
                    0,
                    TCell(
                        ctx!!.resources!!.getString(R.string.rpt_total_amount),
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        1,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    1,
                    TCell(
                        "${df2.format(entityEo.sl_total_amount)}",
                        9F,
                        false,
                        80F,
                        "",
                        Element.ALIGN_RIGHT,
                        1
                    )
                )
                srows.add(CTable(tbl))
                // row 3
                val tDiscount =
                    if (entityEo.sl_total_discount == null) 0.00 else entityEo.sl_total_discount
                tbl = hashMapOf()
                tbl.put(
                    0,
                    TCell(
                        ctx!!.resources!!.getString(R.string.rpt_total_discount),
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        1,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    1,
                    TCell("${df2.format(tDiscount)}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1)
                )
                srows.add(CTable(tbl))
                // row 4
                tbl = hashMapOf()
                tbl.put(
                    0,
                    TCell(
                        ctx!!.resources!!.getString(R.string.rpt_net_amount),
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        1,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    1,
                    TCell(
                        "${df2.format(entityEo.sl_net_amount)}",
                        9F,
                        false,
                        80F,
                        "",
                        Element.ALIGN_RIGHT,
                        1
                    )
                )
                srows.add(CTable(tbl))

                //sl_customer_balance
                var balance: Double = 0.00
                if (entityEo.sl_customer_balance != null) balance = entityEo.sl_customer_balance!!
                tbl = hashMapOf()
                tbl.put(
                    0,
                    TCell(
                        ctx!!.resources!!.getString(R.string.rpt_cu_balance),
                        9F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_RIGHT,
                        1,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    1,
                    TCell(
                        "${df2.format(balance)}  ${entityEo.sl_cr_name}",
                        9F,
                        false,
                        80F,
                        "",
                        Element.ALIGN_RIGHT,
                        1
                    )
                )
                srows.add(CTable(tbl))

                val scw: java.util.ArrayList<Int> = arrayListOf(80, 20)
                summary.add(HeaderFooterRow(0, srows, null, cellsWidth = scw))

                summary.add(
                    HeaderFooterRow(
                        1,
                        null,
                        "T",
                        fontSize = 20F,
                        fontColor = BaseColor.WHITE
                    )
                )
                summary.add(
                    HeaderFooterRow(
                        2,
                        null,
                        "T",
                        fontSize = 20F,
                        fontColor = BaseColor.WHITE
                    )
                )
                summary.add(
                    HeaderFooterRow(
                        3,
                        null,
                        "T",
                        fontSize = 20F,
                        fontColor = BaseColor.WHITE
                    )
                )
                summary.add(
                    HeaderFooterRow(
                        4,
                        null,
                        "T",
                        fontSize = 20F,
                        fontColor = BaseColor.WHITE
                    )
                )
                srows = arrayListOf()
                tbl = hashMapOf()
                tbl.put(
                    0,
                    TCell(
                        ctx!!.resources.getString(R.string.rpt_person_reciever_sig),
                        10F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_CENTER,
                        0,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    1,
                    TCell(
                        ctx!!.resources.getString(R.string.rpt_storekeeper_sig),
                        10F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_CENTER,
                        0,
                        fontName = fontNameAr
                    )
                )
                tbl.put(
                    2,
                    TCell(
                        ctx!!.resources.getString(R.string.rpt_sales_manager_sig),
                        10F,
                        false,
                        12F,
                        "",
                        Element.ALIGN_CENTER,
                        0,
                        fontName = fontNameAr
                    )
                )
                srows.add(CTable(tbl))

                summary.add(HeaderFooterRow(5, srows, null, cellsWidth = arrayListOf(35, 35, 34)))
                val act = activity!!
                GeneratePdf().createPdf(
                    act,
                    imgLogo,
                    entityEo.items,
                    rowHeader,
                    header,
                    footer,
                    null,
                    summary,
                    isRTL
                ) { _, path ->
                   msgListener!!.onSuccess("Pdf Created Successfully")
                    GeneratePdf().printPDF(act, path)
                }
            } catch (e: Exception) {
                msgListener!!.onFailure("Error Exception ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun doPrintOrder(entityEo: Sale_Order){
        if(App.prefs.printing_type == "R") {
            try {
                val lang = Locale.getDefault().toString().toLowerCase()
                var bmp : Bitmap? = null
                ImageLoader().LoadImageFromUrl(URL_LOGO + "co_black_logo.png") {
                    bmp = it
                    var tickets : List<SunmiTicket> = arrayListOf()
                    if(App.prefs.printing_so_mode == "H") {
                        tickets = GenerateTicket(ctx!!, lang).createSunmiTicket(entityEo, bmp,  "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION" )
                    }
                    else{
                        tickets = GenerateTicket(ctx!!, lang).createSunmiTicket(entityEo, bmp,  "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION", "", "" )
                    }
                    SunmiPrintHelper.getInstance().printReceipt(ctx!!, tickets)
                    msgListener!!.onSuccess("Print Successfully")
                }
            }catch (e: Exception){
                msgListener!!.onFailure("Error Exception ${e.message}")
                e.printStackTrace()
            }

        }
    }
}