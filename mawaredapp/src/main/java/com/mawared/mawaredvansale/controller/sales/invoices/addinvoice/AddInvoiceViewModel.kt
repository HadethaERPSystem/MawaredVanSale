package com.mawared.mawaredvansale.controller.sales.invoices.addinvoice

import android.content.Context
import android.location.Location
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.md.*
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Items
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.invoices.IInvoiceRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
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
    var isRunning: Boolean = false
    // google map location GPS
    var location: Location? = null

    var _baseEo: MutableLiveData<Sale> = MutableLiveData()
    var allowed_discount: MutableLiveData<Boolean> = MutableLiveData(false)
    var allowed_select_prod: MutableLiveData<Boolean> = MutableLiveData(false)
    var allowed_enter_gift_qty: MutableLiveData<Boolean> = MutableLiveData(false)

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
            masterDataRepository.getDiscountItem(it, LocalDate.now(), App.prefs.saveUser!!.org_Id)
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
                val totalDiscount: Double = tmpInvoiceItems.sumByDouble { if(it.sld_dis_value == null) 0.0 else it.sld_dis_value!! }
                val netAmount: Double = tmpInvoiceItems.sumByDouble { it.sld_net_total!! }
                val dtFull = docDate.value + " " + LocalTime.now()
                val doc_num = docNo.value?.toInt() ?: 0
                val cu_Id = selectedCustomer?.cu_ref_Id ?: _entityEo?.sl_customerId
                val price_cat_Id = selectedCustomer?.cu_price_cat_Id ?: _entityEo?.sl_price_cat_Id
                // paid
                val amountUSD = if(paidUSD.value == null) 0.0 else paidUSD.value!!.toDouble()
                val change_USD = if(changeUSD.value == null) 0.0 else changeUSD.value!!.toDouble()
                val amountIQD = if(paidIQD.value == null) 0.0 else paidIQD.value!!.toDouble()
                val change_IQD = if(changeIQD.value == null) 0.0 else changeIQD.value!!.toDouble()
                val baseEo = Sale(
                    doc_num, dtFull, "${voucher?.vo_prefix}","", user.cl_Id, user.org_Id, voucher!!.vo_Id,  cu_Id,
                    _sm_id, null, totalAmount, totalDiscount, netAmount, user.ss_cr_Id, user.sf_cr_Id, rate,false,
                    location?.latitude, location?.longitude, price_cat_Id, amountUSD, change_USD, amountIQD, change_IQD,
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

    fun setTotals(){
        totalAmount.postValue(invoiceItems.value!!.sumByDouble{ it.sld_line_total ?: 0.0 } )
        totalDiscount.postValue(invoiceItems.value!!.sumByDouble { it.sld_dis_value  ?: 0.0 })
        val net = invoiceItems.value!!.sumByDouble { it.sld_net_total ?: 0.0 }
        netTotal.postValue(net)
        if(rate != 0.0){
            if(user.ss_cr_code == "$")
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
       val netAmount: Double = tmpInvoiceItems.sumByDouble { it.sld_net_total!! }


       var paidAmount = 0.0
       if (user.ss_cr_code == "IQD") {
           paidAmount = ((amountUSD * rate) + amountIQD) - ((change_USD * rate) + change_IQD)
       } else {
           paidAmount = (amountUSD + (amountIQD / rate)) - (change_USD + (change_IQD / rate))
       }
       remainAmount.value = (netAmount - paidAmount)
       if(rate != 0.0){
           if(user.ss_cr_code == "$")
               fc_remainAmount.postValue( (netAmount - paidAmount) * rate)
           else
               fc_remainAmount.postValue( (netAmount - paidAmount) / rate)
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
        val amountUSD = if(paidUSD.value == null) 0.0 else paidUSD.value!!.toDouble()
        val change_USD = if(changeUSD.value == null) 0.0 else changeUSD.value!!.toDouble()
        val amountIQD = if(paidIQD.value == null) 0.0 else paidIQD.value!!.toDouble()
        val change_IQD = if(changeIQD.value == null) 0.0 else changeIQD.value!!.toDouble()
        val netAmount: Double = tmpInvoiceItems.sumByDouble { it.sld_net_total!! }

        if(netAmount > 0){
            if(selectedCustomer != null){
                if(selectedCustomer!!.payCode!!.contains("CREDIT")) {
                    if (selectedCustomer?.cu_credit_limit != null && selectedCustomer?.cu_credit_limit != 0.0) {

                        val bal = selectedCustomer!!.cu_balance ?: 0.0
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
                            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_remain_amount)
                        }
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

            if(disPer.value != null && disPer.value!!.length > 0){
                lDisPer =  disPer.value!!.toDouble()
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
            val netTotal = (lineTotal - disValue)
            val price_afd = (lDisPer / 100) * unitPrice

            val user = App.prefs.saveUser

            if (mItem == null) {
                rowNo++
                val item = Sale_Items(0, rowNo, null, selectedProduct!!.pr_Id,
                   selectedProduct!!.pr_uom_Id, newQty, 1.0, newQty, gQty,
                    unitPrice, price_afd, lineTotal, lDisPer, disValue, netTotal, null, null, null,
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

            if (disPer.value != null && disPer.value!!.length > 0) {
                val tmpDisPer = disPer.value!!.toDouble()
                val disPerLimit = App.prefs.saveUser!!.dis_Per ?: 0.0

                if (tmpDisPer > disPerLimit) {
                    val str: String =
                        ctx!!.resources!!.getString(R.string.msg_error_discount_overflow)
                    msg += (if (msg!!.length > 0) "\n\r" else "") + String.format(
                        str,
                        App.prefs.saveUser!!.dis_Per!!
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
}