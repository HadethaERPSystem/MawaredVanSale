package com.mawared.mawaredvansale.controller.sales.invoices.addinvoice

import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.location.Location
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
import com.mawared.mawaredvansale.controller.common.TicketPrinting
import com.mawared.mawaredvansale.controller.common.printing.*
import com.mawared.mawaredvansale.data.db.entities.md.*
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Items
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.invoices.IInvoiceRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.ImageLoader
import com.mawared.mawaredvansale.utilities.URL_LOGO
import com.mawared.mawaredvansale.utils.SunmiPrintHelper
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import java.util.*

class AddInvoiceViewModel(private val saleRepository: IInvoiceRepository,
                          private val masterDataRepository: IMDataRepository) : BaseViewModel() {

    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    private val _wr_id: Int = if(App.prefs.savedSalesman?.sm_warehouse_id != null)  App.prefs.savedSalesman!!.sm_warehouse_id!! else 0
    private val user = App.prefs.saveUser!!

    var mode: String = "Add"
    var msgListener: IMessageListener? = null
    var visible = View.VISIBLE
    var addNavigator: IAddNavigator<Sale_Items>? = null
    var ctx: Context? = null
    var activity: Activity? = null
    var isRunning: Boolean = false
    // google map location GPS
    var location: Location? = null

    var _baseEo: MutableLiveData<Sale> = MutableLiveData()
    var allowed_discount: MutableLiveData<Boolean> = MutableLiveData(false)
    var allowed_select_prod: MutableLiveData<Boolean> = MutableLiveData(false)
    var allowed_enter_gift_qty: MutableLiveData<Boolean> = MutableLiveData(false)
    var ccustomer_name: MutableLiveData<String> = MutableLiveData()

    private var tmpInvoiceItems: ArrayList<Sale_Items> = arrayListOf()
    private var tmpDeletedItems: ArrayList<Sale_Items> = arrayListOf()

    private val _invoiceItems = MutableLiveData<List<Sale_Items>>()
    val invoiceItems: LiveData<List<Sale_Items>>
        get() = _invoiceItems

    var selectedCustomer: Customer? = null
    var oCu_Id: Int? = null
    var selectedProduct: Product? = null

    var rowNo: Int = 0
    var docNo = MutableLiveData<String>()
    var docDate = MutableLiveData<String>()
    var totalAmount : MutableLiveData<Double> = MutableLiveData()
    var netTotal: MutableLiveData<Double> = MutableLiveData()
    var totalDiscount: MutableLiveData<Double> = MutableLiveData()
    var notes = MutableLiveData<String>()

    //var isGift: MutableLiveData<Boolean> = MutableLiveData(false)
    var prom_qty: Double = 0.0
    var prom_ex_qty: Double = 0.0
    var paidUSD = MutableLiveData<String>()
    var changeUSD= MutableLiveData<String>()
    var paidIQD= MutableLiveData<String>()
    var changeIQD= MutableLiveData<String>()
    var remainAmount = MutableLiveData<Double>()
    var fc_remainAmount = MutableLiveData<Double>()
    var scr_symbol : MutableLiveData<String> = MutableLiveData(App.prefs.saveUser!!.ss_cr_code!!)
    var fcr_symbol : MutableLiveData<String> = MutableLiveData(App.prefs.saveUser!!.sf_cr_code!!)
    var fc_amount: MutableLiveData<Double> = MutableLiveData()

    var searchQty: MutableLiveData<String> = MutableLiveData("1")
    var searchBarcode: MutableLiveData<String> = MutableLiveData()
    var giftQty: MutableLiveData<String> = MutableLiveData("")
    var disPer: MutableLiveData<String> = MutableLiveData("")
    var discAmnt: MutableLiveData<String> = MutableLiveData("")

    var cr_symbol: MutableLiveData<String> = MutableLiveData(App.prefs.saveUser?.ss_cr_code ?: "")
    var _entityEo: Sale? = null
    private val sl_id : MutableLiveData<Int> = MutableLiveData()
    val entityEo: LiveData<Sale> = Transformations
        .switchMap(sl_id){
            saleRepository.getInvoice(it)
        }

    var term: MutableLiveData<String> = MutableLiveData()
    val customerList : LiveData<List<Customer>> = Transformations.switchMap(term) {
        masterDataRepository.customers_getSchedule(_sm_id, it)
    }

    private val _term: MutableLiveData<String> = MutableLiveData()
    val productList: LiveData<List<Product>> = Transformations
        .switchMap(_term){
            masterDataRepository.getProducts(it, App.prefs.savedSalesman?.sm_warehouse_id, price_cat_code)
    }

    val networkState: LiveData<NetworkState> by lazy {
        saleRepository.networkState
    }

    var rate : Double = 0.0
    private val _cr_Id: MutableLiveData<Int> = MutableLiveData()
    val currencyRate: LiveData<Currency_Rate> = Transformations
        .switchMap(_cr_Id) {
        masterDataRepository.getRate(it)
    }

    var voucher: Voucher? = null
    private val _vo_code: MutableLiveData<String> = MutableLiveData()
    val mVoucher: LiveData<Voucher> =Transformations
        .switchMap(_vo_code){
        masterDataRepository.getVoucherByCode(it)
    }

    var unitPrice : Double = 0.0
    var price_cat_code = "POS"
    private val _prod_Id: MutableLiveData<Int> = MutableLiveData()

    var discount: Discount? = null
    val mDiscount: LiveData<Discount> = Transformations
        .switchMap(_prod_Id){
            masterDataRepository.getDiscountItem(it, LocalDate.now(), App.prefs.saveUser!!.org_Id, price_cat_code)
        }

    var ageDebit: Customer ?= null
    fun loadAgeDebit(cu_Id: Int){
        try {
            Coroutines.ioThenMain({ageDebit = masterDataRepository.customer_getAgeDebit(cu_Id)},{})

        }catch (e: java.lang.Exception){
            e.printStackTrace()
        }
    }
    //------------- set function
    fun setInvoiceId(id: Int){
        if(sl_id.value == id){
            return
        }
        sl_id.value = id
    }

    fun setTerm(term: String){
        if(_term.value == term){
            return
        }
        _term.value = term
    }

    fun setVoucherCode(vo_code: String){
        if(_vo_code.value == vo_code){
            return
        }
        _vo_code.value = vo_code
    }

    fun setCurrencyId(cr_id: Int){
        if(_cr_Id.value == cr_id){
            return
        }
        _cr_Id.value = cr_id
    }

    fun setProductId(prod_Id: Int){
//        if(_prod_Id.value == prod_Id){
//            return
//        }
        _prod_Id.value = prod_Id
    }

    fun setPriceCategory(){
        price_cat_code = if(selectedCustomer != null && selectedCustomer?.cu_price_cat_code != null) selectedCustomer!!.cu_price_cat_code!! else "POS"
    }

    fun setItems(items: List<Sale_Items>?){
        if(items != null && _invoiceItems == items){
            return
        }
        _invoiceItems.value = items ?: arrayListOf()
        if(items != null) {
            tmpInvoiceItems.addAll(items)

        }
    }
    //---------------------
    //---- button function

    fun onSave(Success:(() -> Unit) = {}, Fail:(() -> Unit) = {}) {
        if (isValid()) {
            try {
                isRunning = true
                val user = App.prefs.saveUser!!
                val strDate = LocalDateTime.now()
                val totalAmount: Double = tmpInvoiceItems.sumByDouble { it.sld_line_total!! }
                val totalDiscount: Double = tmpInvoiceItems.sumByDouble { if(it.sld_dis_value == null) 0.0 else it.sld_dis_value!! + if(it.sld_disc_amnt == null) 0.0 else it.sld_disc_amnt!! }
                val netAmount: Double = tmpInvoiceItems.sumByDouble { it.sld_net_total!! }
                val dtFull = docDate.value + " " + LocalTime.now()
                val doc_num = docNo.value?.toInt() ?: 0
                val cu_Id = selectedCustomer?.cu_ref_Id ?: _entityEo?.sl_customerId
                val price_cat_Id = selectedCustomer?.cu_price_cat_Id ?: _entityEo?.sl_price_cat_Id
                // paid
                val amountUSD = if(paidUSD.value.isNullOrEmpty()) 0.0 else paidUSD.value!!.toDouble()
                val change_USD = if(changeUSD.value.isNullOrEmpty()) 0.0 else changeUSD.value!!.toDouble()
                val amountIQD = if(paidIQD.value.isNullOrEmpty()) 0.0 else paidIQD.value!!.toDouble()
                val change_IQD = if(changeIQD.value.isNullOrEmpty()) 0.0 else changeIQD.value!!.toDouble()
                //val cuId = if(App.prefs.saveUser!!.sl_cr_code!! == App.prefs.saveUser!!.ss_cr_code!!) App.prefs.saveUser!!.sf_cr_Id!! else App.prefs.saveUser!!.ss_cr_Id!!
                val baseEo = Sale(
                    doc_num, dtFull, "${voucher?.vo_prefix}","", user.cl_Id, user.org_Id, voucher!!.vo_Id,  cu_Id, ccustomer_name.value,
                    _sm_id, null, totalAmount, totalDiscount, netAmount, 0.0, user.ss_cr_Id, user.sf_cr_Id, rate,false,
                    location?.latitude, location?.longitude, price_cat_Id, amountUSD, change_USD, amountIQD, change_IQD, notes.value,
                    "$strDate", "${user.id}", "$strDate", "${user.id}"
                )
                baseEo.sl_price_cat_code = price_cat_code
                if(mode != "Add"){
                    baseEo.sl_Id = _entityEo!!.sl_Id
                    baseEo.created_at = _entityEo!!.created_at
                    baseEo.created_by = _entityEo!!.created_by
                    baseEo.sl_rate = _entityEo!!.sl_rate
                }
                baseEo.items.addAll(tmpInvoiceItems)
                if(tmpDeletedItems.count() > 0){
                    baseEo.items_deleted.addAll(tmpDeletedItems)
                }
                Coroutines.main {
                    try {
                        val response = saleRepository.SaveOrUpdate(baseEo)
                        if(response.isSuccessful){
                            _baseEo.value = response.data!!
                            Success()
                        }
                        else{
                            msgListener?.onFailure("Error message when try to save sale invoice. Error is ${response.message}")
                            Fail()
                        }
                    }catch (e: Exception){
                        msgListener?.onFailure("Error message when try to save sale invoice. Error is ${e.message}")
                        Fail()
                    }
                }
            }catch (e: Exception){
                msgListener?.onFailure("${ctx!!.resources!!.getString(R.string.msg_exception)} Exception is ${e.message}")
                Fail()
            }
        }else{
            Fail()
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

    fun setTotals(){
        totalAmount.postValue(invoiceItems.value!!.sumByDouble{ it.sld_line_total ?: 0.0 } )
        totalDiscount.postValue(invoiceItems.value!!.sumByDouble { (it.sld_dis_value  ?: 0.0) + (it.sld_disc_amnt ?: 0.0) })
        val net = invoiceItems.value!!.sumByDouble { it.sld_net_total ?: 0.0 }
        netTotal.postValue(net)
        if(rate != 0.0){
            if(user.isDirectRate == "Y")
                fc_amount.postValue(net / rate)
            else
                fc_amount.postValue(net * rate)

        }
    }

   fun updateRemain(){
       val amountUSD = if(paidUSD.value.isNullOrEmpty()) 0.0 else paidUSD.value!!.toDouble()
       val change_USD = if(changeUSD.value.isNullOrEmpty()) 0.0 else changeUSD.value!!.toDouble()
       val amountIQD = if(paidIQD.value.isNullOrEmpty()) 0.0 else paidIQD.value!!.toDouble()
       val change_IQD = if(changeIQD.value.isNullOrEmpty()) 0.0 else changeIQD.value!!.toDouble()
       val netAmount: Double = tmpInvoiceItems.sumByDouble { it.sld_net_total!! }

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

    private fun isValid(): Boolean {
        var isSuccess = true
        var msg: String? = ""
        if (docDate.value == null) {
            msg =  ctx!!.resources!!.getString(R.string.msg_error_invalid_date)
        }
        if (selectedCustomer == null && _entityEo == null) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_customer)
        }

        if (tmpInvoiceItems.count() == 0) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_items)
        }

        if (App.prefs.saveUser == null) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_currency)
        }
        val amountUSD = if(paidUSD.value.isNullOrEmpty()) 0.0 else paidUSD.value!!.toDouble()
        val change_USD = if(changeUSD.value.isNullOrEmpty()) 0.0 else changeUSD.value!!.toDouble()
        val amountIQD = if(paidIQD.value.isNullOrEmpty()) 0.0 else paidIQD.value!!.toDouble()
        val change_IQD = if(changeIQD.value.isNullOrEmpty()) 0.0 else changeIQD.value!!.toDouble()
        val netAmount: Double = tmpInvoiceItems.sumByDouble { it.sld_net_total!! }
        val paidAmount = getPaidAmount(amountUSD, amountIQD, change_USD, change_IQD)
        if(netAmount > 0){
            if(selectedCustomer != null){
                if(selectedCustomer!!.payCode!!.contains("CREDIT")) {
                    if (selectedCustomer?.cu_credit_limit != null && selectedCustomer?.cu_credit_limit != 0.0) {

                        val bal = selectedCustomer!!.cu_balance ?: 0.0
                        //val user = App.prefs.saveUser!!

                        val amount = (bal) + netAmount - paidAmount
//                        if(amount >= user.dropAmnt!! || amount <= -user.dropAmnt!!){
//                            if(user.dropAmnt!! != 0.0){
//                                msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_remain_amount)
//                            }else if(amount != 0.0){
//                                msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_remain_amount)
//                            }
//                        }
                        if (selectedCustomer!!.cu_credit_limit!! < amount) {
                            msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_credit_limit)
                        }
                    }

                    var cu_AgeDebit : Int = 0
                    if(ageDebit != null){
                        cu_AgeDebit = ageDebit!!.cu_DebitAge ?: 0
                    }
                    if (selectedCustomer?.cu_credit_days != null && selectedCustomer?.cu_credit_days != 0) {
                        if (cu_AgeDebit > selectedCustomer!!.cu_credit_days!!) {
                            msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_credit_limit)
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
                        if(user.dropAmnt!! != 0.0){
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

    // create new invoice
    fun onNew() {
        val strDate: LocalDate = LocalDate.now()

        docNo.value = "0"
        docDate.value = returnDateString(strDate.toString())
        selectedProduct = null
        selectedCustomer = null
        searchBarcode.value = ""
        searchQty.value = "1"
        unitPrice = 0.00
        //isGift.value = false
        price_cat_code = "POS"
        tmpInvoiceItems.clear()
        clear("cu")
        clear("prod")
    }

    //-------------------------------------------
    //---- row function
    fun onAddItem() {

        if (isValidRow()) {
            try {
                createRow { complete ->
                    if(complete){
                        // reset item
                        selectedProduct = null
                        searchBarcode.value = ""
                        searchQty.value = "1"
                        giftQty.value = ""
                        disPer.value = ""
                        discAmnt.value = ""
                        //isGift.value = false
                        unitPrice = 0.0
                        prom_qty = 0.0
                        prom_ex_qty = 0.0
                        allowed_discount.value = false
                        allowed_enter_gift_qty.value = false
                        clear("prod")
                    }else{
                        msgListener?.onFailure(ctx!!.resources!!.getString(R.string.msg_error_fail_add_item))
                    }
                }
            }catch (e: Exception){
                msgListener?.onFailure("${ctx!!.resources!!.getString(R.string.msg_error_add_item)} : ${e.message}")
            }
        }
    }

    private fun createRow(complete:(Boolean) -> Unit) {
        try {

            val mItem = tmpInvoiceItems.find { it.sld_prod_Id == selectedProduct!!.pr_Id && it.sld_batch_no == selectedProduct!!.pr_batch_no && it.sld_expiry_date == selectedProduct!!.pr_expiry_date }

            val strDate = LocalDateTime.now()
            var qty : Double = if(!searchQty.value.isNullOrEmpty()) searchQty.value!!.toDouble() else 0.0
            if(mItem?.sld_pack_qty != null)
               qty += mItem.sld_pack_qty!!

            val gQty: Double = (if(!giftQty.value.isNullOrEmpty()) giftQty.value!!.toDouble() else 0.0)
            val ogQty: Double = if(mItem != null) mItem.sld_gift_qty!! else 0.0

            val newQty = qty + gQty
            //val lineTotal = if(isGift.value == true) 0.0 else unitPrice * qty
            val lineTotal = unitPrice * newQty

            //val gdisValue = if(isGift.value == true) 0.0 else unitPrice * gQty
            val gdisValue =  unitPrice * (gQty + ogQty)
            val gdis_per = ((gdisValue / lineTotal) * 100)
            //val newQty = qty + if(isGift.value == true) 0.0 else gQty


            var disValue = 0.0
            var lDisPer: Double = 0.0
            var _discAmnt: Double = 0.0
            //var addDiscPrcnt: Double = 0.0

            if(disPer.value != null && disPer.value!!.length > 0){
                lDisPer =  disPer.value!!.toDouble()
            }

            if(discAmnt.value != null && discAmnt.value!!.length > 0){
                _discAmnt = discAmnt.value!!.toDouble()
            }
            disValue = (lDisPer / 100) * lineTotal

            // check if there is discount on item
            if(discount != null /*&& isGift.value == false*/){
                if(discount!!.pr_dis_type == "P"){
                    lDisPer = discount!!.pr_dis_value!!
                    disValue = (lDisPer / 100) * lineTotal
                }else{
                    lDisPer = (discount!!.pr_dis_value!! / lineTotal) * 100
                    disValue = discount!!.pr_dis_value!! * qty
                }
            }

            if(gdisValue != 0.0){
                lDisPer += gdis_per
                disValue += gdisValue
            }

            //val netTotal = if(isGift.value == true) 0.0 else (lineTotal - disValue)
            val netTotal = (lineTotal - (disValue + _discAmnt))

            val price_afd =  (1 - (lDisPer / 100)) * unitPrice // * if(lDisPer == 0.0) 1.0 else (lDisPer / 100)

            val user = App.prefs.saveUser

            if (mItem == null) {
                rowNo++
                val item = Sale_Items(0, rowNo, null, selectedProduct!!.pr_Id,
                   selectedProduct!!.pr_uom_Id, newQty, 1.0, newQty, gQty,
                    unitPrice, price_afd, lineTotal, lDisPer, disValue, 0.0, 0.0, _discAmnt, netTotal, null, null, null,
                    _wr_id,selectedProduct!!.pr_batch_no, selectedProduct!!.pr_expiry_date,  selectedProduct!!.pr_mfg_date,
                    false,"$strDate","${user?.id}", "$strDate", "${user?.id}"
                )
                item.sld_prod_name = selectedProduct!!.pr_description
                item.sld_prod_name_ar = selectedProduct!!.pr_description_ar
                item.sld_barcode =  selectedProduct!!.pr_barcode

                tmpInvoiceItems.add(item)
            } else {
                mItem.sld_pack_qty = newQty
                mItem.sld_unit_qty = newQty
                mItem.sld_gift_qty =  gQty + ogQty
                mItem.sld_line_total = lineTotal
                mItem.sld_net_total = netTotal
                mItem.sld_dis_per = lDisPer
                mItem.sld_dis_value = disValue
                mItem.sld_disc_amnt = _discAmnt
                mItem.updated_at = "$strDate"
            }
            _invoiceItems.postValue(tmpInvoiceItems)
            complete(true)

        }catch (e: Exception){
            complete(false)
            msgListener?.onFailure("${ctx!!.resources!!.getString(R.string.msg_error_add_item)} : ${e.message}")
        }
    }

    private fun isValidRow(): Boolean {
        var isSuccessful = true
        var msg: String? = ""
        var tQty = 0.0

        if (selectedProduct == null) {
            msg = ctx!!.resources!!.getString(R.string.msg_error_invalid_product)
        }else {


            val mItem =
                tmpInvoiceItems.find { it.sld_prod_Id == selectedProduct!!.pr_Id && it.sld_batch_no == selectedProduct!!.pr_batch_no && it.sld_expiry_date == selectedProduct!!.pr_expiry_date }
            if (mItem != null) {
                tQty += mItem.sld_unit_qty!! //mItem.sld_gift_qty!! + mItem.sld_unit_qty!!
            }

            val gQty: Double =
                (if (!giftQty.value.isNullOrEmpty()) giftQty.value!!.toDouble() else 0.0) + (if (mItem != null) mItem.sld_gift_qty!!.toDouble() else 0.0)
            val qty: Double =
                if (!searchQty.value.isNullOrEmpty()) searchQty.value!!.toDouble() else 0.0

            tQty += qty + gQty

            val pr_qty: Int =
                if (selectedProduct?.pr_qty != null) selectedProduct?.pr_qty!!.toInt() else 0
            val _discAmnt = if(discAmnt.value.isNullOrEmpty()) 0.0 else discAmnt.value!!.toDouble()
            val _discAmntPrcnt = ((_discAmnt / (unitPrice ?: 1.0)) * 100)
            val _discPrcnt = if(disPer.value.isNullOrEmpty()) 0.0 else disPer.value!!.toDouble()

            if ((_discAmntPrcnt + _discPrcnt) > 0.0) {
                val disPerLimit = App.prefs.saveUser!!.iDiscPrcnt
                if ((_discAmntPrcnt + _discPrcnt) > disPerLimit) {
                    val str: String =
                        ctx!!.resources!!.getString(R.string.msg_error_discount_overflow)
                    msg += (if (msg!!.length > 0) "\n\r" else "") + String.format(
                        str,
                        App.prefs.saveUser!!.iDiscPrcnt
                    )
                }
            }
            if (pr_qty <= 0) {
                msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_not_available_product_qty)
            }

            if (qty <= 0 && gQty <= 0) {
                msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_invalid_qty)
            }

            if (tQty > pr_qty) {
                msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_not_required_qty_less_product_qty)
            }

            if (unitPrice == 0.00) {
                msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_invalid_price)
            }

            if ((qty % 1) != 0.0) {
                msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_qty_has_digit)
            }

            if ((gQty % 1) != 0.0) {
                msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_gift_qty_has_digit)
            }
        }
        if (!msg.isNullOrEmpty()) {
            isSuccessful = false
            msgListener?.onFailure(msg)
        }

        return isSuccessful
    }


    fun onItemDelete(item: Sale_Items) {
        addNavigator?.onDelete(item)
    }

    fun clearItems(){
        tmpInvoiceItems.clear()
        _invoiceItems.postValue(tmpInvoiceItems)
    }

    fun deleteItem(baseEo: Sale_Items){
        if(baseEo.sld_Id != 0){
            // delete from database
            val item = _invoiceItems.value?.find { it.sld_rowNo == baseEo.sld_rowNo }
            if(item != null){
                tmpDeletedItems.add(item)
            }
        }
        // delete from current list
        tmpInvoiceItems.remove(baseEo)
        _invoiceItems.value = _invoiceItems.value?.filter { it.sld_rowNo != baseEo.sld_rowNo }
    }
    //----------------------------------

    fun clear(code: String) {
        when(code) {
            "cu"-> {
                selectedCustomer = null
            }
            "prod"-> {
                selectedProduct = null
            }
        }
        addNavigator?.clear(code)
    }

    fun onDatePicker(v: View) {
        addNavigator?.onShowDatePicker(v)
    }

    fun getProductName(item: Sale_Items): String?{
        val lang =  Locale.getDefault().toString()
        when(lang.toLowerCase()){
            "en_us" -> return item.sld_prod_name
            "ar_iq" -> return item.sld_prod_name_ar
            else -> return item.sld_prod_name_ar
        }
    }

    fun cancelJob(){
        masterDataRepository.cancelJob()
        saleRepository.cancelJob()
    }

    fun doPrint(entityEo: Sale){
        if(App.prefs.printing_type == "R") {
            try {
                val lang = Locale.getDefault().toString().toLowerCase()
                entityEo.sl_salesman_phone = App.prefs.savedSalesman?.sm_phone_no ?: ""

                if(App.prefs.printer == "Sunmi"){
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
                        SunmiPrintHelper.getInstance().printReceipt(ctx, tickets)
                        msgListener?.onSuccess("Print Successfully")
                    }
                } else {

                    val tickets = GenerateTicket(ctx!!, lang).create(entityEo, URL_LOGO + "co_black_logo.png", "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION", null, null)

                    TicketPrinting(ctx!!, tickets).run()
                    msgListener?.onSuccess("Print Successfully")

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
                var LineNum: Int = 0
                if(!App.prefs.saveUser!!.print_msg.isNullOrEmpty()){
                    val lines = App.prefs.saveUser!!.print_msg!!.split("#").map{it.trim()}
                    for (str: String in lines){
                        footer.add(HeaderFooterRow(LineNum, null, "$str", fontSize = 11F, align = Element.ALIGN_LEFT, Font.BOLD,  fontName = fontNameAr))
                        LineNum++
                    }
                    footer.add(HeaderFooterRow(LineNum++, null, "", fontSize = 12F, align = Element.ALIGN_LEFT,  fontName = fontNameAr))
                    footer.add(HeaderFooterRow(LineNum++, null, "", fontSize = 12F, align = Element.ALIGN_LEFT,  fontName = fontNameAr))
                    footer.add(HeaderFooterRow(LineNum++, null, "", fontSize = 12F, align = Element.ALIGN_LEFT,  fontName = fontNameAr))
                }

                footer.add(HeaderFooterRow(LineNum++,null,"موارد / الشركة الحديثة للبرامجيات الاتمتة المحدودة", fontSize = 9F, align = Element.ALIGN_LEFT, fontName = fontNameAr))
                footer.add( HeaderFooterRow( LineNum++,null,ctx!!.resources!!.getString(R.string.rpt_user_name) + ": ${App.prefs.saveUser!!.name}",  fontSize = 9F, align = Element.ALIGN_LEFT, fontName = fontNameAr))

                val rowHeader: HashMap<Int, RowHeader> = hashMapOf()
                rowHeader.put(0, RowHeader("#", 9.0F, false, 4, "", 0, 0F))
                rowHeader.put(  1,   RowHeader(   ctx!!.resources!!.getString(R.string.rpt_barcode),   9.0F, false,   15,  "", 0,   0F         )          )
                rowHeader.put( 2,  RowHeader(   ctx!!.resources!!.getString(R.string.rpt_prod_name),   9.0F,  false, 37, "", 0, 0F  )            )
                rowHeader.put(  3,   RowHeader(   ctx!!.resources!!.getString(R.string.rpt_qty),  9.0F,  false,  5,  "",  0,  0F                )            )
                rowHeader.put(  4,   RowHeader(   ctx!!.resources!!.getString(R.string.rpt_uom),  9.0F,  false,  8,  "",  0,  0F                )            )
                rowHeader.put( 5, RowHeader(  ctx!!.resources!!.getString(R.string.rpt_gift),  9.0F, false,5,  "", 0, 0F )  )
                rowHeader.put( 6, RowHeader(  ctx!!.resources!!.getString(R.string.rpt_unit_price), 9.0F,  false,11,  "", 0,  0F  )           )
                //rowHeader.put( 7,  RowHeader(   ctx!!.resources!!.getString(R.string.rpt_dis_value),  9.0F,   false,  7,    "",    0,  0F) )
                rowHeader.put(    7,   RowHeader(   ctx!!.resources!!.getString(R.string.rpt_net_total),   9.0F,  false,  11,  "",     0,    0F       )     )
                rowHeader.put( 8,
                    RowHeader( ctx!!.resources!!.getString(R.string.rpt_notes),  9.0F,  false,  13,  "Total",   0,  0F           )            )


                // Summary part
                val df1 = DecimalFormat("#,###")
                val df2 = DecimalFormat("#,###,###.#")
                val summary: ArrayList<HeaderFooterRow> = arrayListOf()
                tbl = hashMapOf()
                var srows: ArrayList<CTable> = arrayListOf()
                val tQty = entityEo.items.sumByDouble { it.sld_pack_qty!! }
                tbl.put(    0,    TCell(   ctx!!.resources!!.getString(R.string.rpt_total_qty),   9F,  false,  25F,   "",   Element.ALIGN_RIGHT,  1,  fontName = fontNameAr)          )
                tbl.put(1, TCell("${df1.format(tQty)}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1))
                srows.add(CTable(tbl))

                tbl = hashMapOf()
                val tweight =  entityEo.items.sumByDouble { if (it.sld_total_weight == null) 0.00 else it.sld_total_weight!! }
                tbl.put(  0, TCell( ctx!!.resources!!.getString(R.string.rpt_total_weight), 9F,   false, 12F,  "",  Element.ALIGN_RIGHT,    1,    fontName = fontNameAr  ) )
                tbl.put(  1,  TCell("${df2.format(tweight)}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1)            )
                srows.add(CTable(tbl))
                // row 2
                tbl = hashMapOf()
                tbl.put(   0,  TCell(  ctx!!.resources!!.getString(R.string.rpt_total_amount),  9F,   false,  12F,  "",   Element.ALIGN_RIGHT,  1,   fontName = fontNameAr ) )
                tbl.put( 1,  TCell( "${df2.format(entityEo.sl_total_amount)}",   9F,    false, 80F, "",  Element.ALIGN_RIGHT, 1 ) )
                srows.add(CTable(tbl))
                // row 3
                val tDiscount =
                    if (entityEo.sl_total_discount == null) 0.00 else entityEo.sl_total_discount
                tbl = hashMapOf()
                tbl.put( 0,  TCell(  ctx!!.resources!!.getString(R.string.rpt_total_discount), 9F, false,  12F,  "",  Element.ALIGN_RIGHT, 1, fontName = fontNameAr     )  )
                tbl.put(   1, TCell("${df2.format(tDiscount)}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1)          )
                srows.add(CTable(tbl))
                // row 4
                tbl = hashMapOf()
                tbl.put(  0,  TCell(  ctx!!.resources!!.getString(R.string.rpt_net_amount),9F,   false, 12F, "",  Element.ALIGN_RIGHT,  1, fontName = fontNameAr  )  )
                tbl.put( 1,  TCell( "${df2.format(entityEo.sl_net_amount)}", 9F, false,  80F,  "",  Element.ALIGN_RIGHT,  1  ) )
                srows.add(CTable(tbl))

                //sl_customer_balance
                var balance: Double = 0.00
                if (entityEo.sl_customer_balance != null) balance = entityEo.sl_customer_balance!!
                tbl = hashMapOf()
                tbl.put(   0,  TCell( ctx!!.resources!!.getString(R.string.rpt_cu_balance), 9F, false, 12F, "",  Element.ALIGN_RIGHT, 1,  fontName = fontNameAr )  )
                tbl.put( 1, TCell( "${df2.format(balance)}  ${entityEo.sl_cr_name}",   9F,   false, 80F,  "",   Element.ALIGN_RIGHT, 1 )  )
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
                tbl.put( 0, TCell( ctx!!.resources.getString(R.string.rpt_person_reciever_sig),10F,false, 12F, "",  Element.ALIGN_CENTER,0,  fontName = fontNameAr   ))
                tbl.put( 1,
                    TCell( ctx!!.resources.getString(R.string.rpt_storekeeper_sig),10F,false,12F, "", Element.ALIGN_CENTER, 0, fontName = fontNameAr ) )
                tbl.put( 2, TCell(  ctx!!.resources.getString(R.string.rpt_sales_manager_sig),  10F,   false, 12F, "", Element.ALIGN_CENTER, 0,  fontName = fontNameAr )  )
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
}