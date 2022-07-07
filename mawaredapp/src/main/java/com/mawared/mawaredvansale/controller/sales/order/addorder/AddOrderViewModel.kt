package com.mawared.mawaredvansale.controller.sales.order.addorder

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
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order_Items
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.order.IOrderRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class AddOrderViewModel(private val orderRepository: IOrderRepository,
                        private val masterDataRepository: IMDataRepository) : BaseViewModel() {

    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    var mode: String = "Add"
    var ctx: Context? = null
    var visible = View.VISIBLE
    // listener
    var msgListener: IMessageListener? = null
    var addNavigator: IAddNavigator<Sale_Order_Items>? = null
    var allowed_discount: MutableLiveData<Boolean> = MutableLiveData(false)
    var allowed_select_prod: MutableLiveData<Boolean> = MutableLiveData(false)
    var allowed_enter_gift_qty: MutableLiveData<Boolean> = MutableLiveData(false)
    var isRunning: Boolean = false
    // google map location GPS
    var location: Location? = null

    var rowNo: Int = 0
    var docNo: MutableLiveData<String> = MutableLiveData()
    var docDate = MutableLiveData<String>()
    var cCustomer_Name = MutableLiveData<String>()
    var totalAmount : MutableLiveData<Double> = MutableLiveData()
    var netTotal: MutableLiveData<Double> = MutableLiveData()
    var totalDiscount: MutableLiveData<Double> = MutableLiveData()
    //var isGift: MutableLiveData<Boolean> = MutableLiveData(false)
    var cr_symbol: MutableLiveData<String> = MutableLiveData(App.prefs.saveUser?.ss_cr_code ?: "")

    var searchQty: MutableLiveData<String> = MutableLiveData("1")
    var searchBarcode: MutableLiveData<String> = MutableLiveData()
    var giftQty: MutableLiveData<String> = MutableLiveData("")
    var disPer: MutableLiveData<String> = MutableLiveData("")

    var selectedCustomer: Customer? = null
    var oCu_Id: Int? = null
    var selectedProduct: Product? = null

    //////////////////////////////////////////////////
    // for save header and detail to database
    val _baseEo: MutableLiveData<Sale_Order> = MutableLiveData()

    // for load order for edit or view
    var _entityEo: Sale_Order? = null
    private val so_id : MutableLiveData<Int> = MutableLiveData()
    val entityEo: LiveData<Sale_Order> = Transformations
        .switchMap(so_id){
            orderRepository.getOrderById(it)
        }

    // for items
    private var tmpDeletedItems: ArrayList<Sale_Order_Items> = arrayListOf()
    private var tmpSOItems: ArrayList<Sale_Order_Items> = arrayListOf()
    private val _soItems = MutableLiveData<List<Sale_Order_Items>>()
    val soItems: LiveData<List<Sale_Order_Items>>
        get() = _soItems

    // for customer
    val term: MutableLiveData<String> = MutableLiveData()
    val customerList : LiveData<List<Customer>> = Transformations.switchMap(term) { masterDataRepository.getCustomers(_sm_id, it)  }

    val networkState by lazy { orderRepository.networkState }

    val _term: MutableLiveData<String> = MutableLiveData()
    val productList: LiveData<List<Product>> = Transformations
        .switchMap(_term){
            masterDataRepository.getProductsByUserWarehouse(it, _sm_id, price_cat_code)
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

    // set function
    fun setOrderId(id: Int){
        if(so_id.value == id){
            return
        }
        so_id.value = id
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
        if(_prod_Id.value == prod_Id){
            return
        }
        _prod_Id.value = prod_Id
    }
    fun setPriceCategory(){
        price_cat_code = if(selectedCustomer != null && selectedCustomer?.cu_price_cat_code != null) selectedCustomer!!.cu_price_cat_code!! else "POS"
    }

    fun setItems(items: List<Sale_Order_Items>?){
        if(items != null && _soItems == items){
            return
        }
        _soItems.value = items ?: arrayListOf()
        if(items != null)
            tmpSOItems.addAll(items)
    }
    //---------------------------------------
    //----- button function
    fun onSave(){
        if(isValid()){
            try {
                isRunning = true
                val user = App.prefs.saveUser!!
                val totalAmount: Double = tmpSOItems.sumByDouble { it.sod_line_total!! }
                val netAmount: Double = tmpSOItems.sumByDouble { it.sod_net_total!! }
                val totalDiscount = tmpSOItems.sumByDouble { it.sod_disvalue!! }
                val strDate= LocalDateTime.now()
                val dtFull = docDate.value + " " + LocalTime.now()
                val doc_num = docNo.value?.toInt() ?: 0
                val cu_Id = selectedCustomer?.cu_ref_Id ?: _entityEo?.so_customerId
                val price_cat_Id = selectedCustomer?.cu_price_cat_Id ?: _entityEo?.so_price_cat_Id
                val baseEo = Sale_Order( user.cl_Id, user.org_Id, doc_num, dtFull,
                    "", mVoucher.value!!.vo_prefix, mVoucher.value!!.vo_Id,
                    _sm_id, cu_Id, cCustomer_Name.value, null, totalAmount, totalDiscount, netAmount, user.ss_cr_Id, rate,
                    false, location?.latitude, location?.longitude, price_cat_Id, "$strDate",
                    "${user.id}", "$strDate", "${user.id}"
                )
                baseEo.so_price_cat_code =  price_cat_code

                if(mode != "Add"){
                    baseEo.so_id = _entityEo!!.so_id
                    baseEo.created_at = _entityEo!!.created_at
                    baseEo.created_by = _entityEo!!.created_by
                    baseEo.so_rate = _entityEo!!.so_rate
                }

                baseEo.items = arrayListOf()
                baseEo.items.addAll(tmpSOItems)
                if(tmpDeletedItems.count() > 0){
                    baseEo.items_deleted.addAll(tmpDeletedItems)
                }
                Coroutines.main {
                    try {
                        val response = orderRepository.SaveOrUpdate(baseEo)
                        if(response.isSuccessful){
                            _baseEo.value = response.data!!
                            isRunning = false
                        }
                        else{
                            msgListener?.onFailure("Error message when try to save order. Error is ${response.message}")
                            isRunning = false
                        }
                    }catch (e: Exception){
                        msgListener?.onFailure("Error message when try to save order. Error is ${e.message}")
                        isRunning = false
                    }
                }

            }catch (e: Exception){
                msgListener?.onFailure("${ctx!!.resources!!.getString(R.string.msg_exception)} Exception is ${e.message}")
                isRunning = false
            }
        }
    }

    private fun isValid(): Boolean{
        var isSuccessful = true
        var msg: String? = ""

        if (docDate.value == null) {
                msg =  ctx!!.resources!!.getString(R.string.msg_error_invalid_date)
        }
        if (selectedCustomer == null && _entityEo == null) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_customer)
        }

        if (tmpSOItems.count() == 0) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_items)
        }

        if (App.prefs.saveUser == null) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_currency)
        }

        val netAmount: Double = tmpSOItems.sumByDouble { it.sod_net_total!! }

        if (selectedCustomer!!.cu_credit_limit!! < netAmount) {
            msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(
                R.string.msg_error_credit_limit)
        }

        var cu_AgeDebit : Int = 0
        if(ageDebit != null){
            cu_AgeDebit = ageDebit!!.cu_DebitAge ?: 0
        }
        if (selectedCustomer?.cu_credit_days != null && selectedCustomer?.cu_credit_days != 0) {
            if (cu_AgeDebit > selectedCustomer!!.cu_credit_days!!) {
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

    fun onNew(){
        val strDate: LocalDate = LocalDate.now()
        docNo.value = ""
        docDate.value = returnDateString(strDate.toString())
        totalAmount.value = 0.0
        netTotal.value = 0.0
        totalDiscount.value = 0.0
        giftQty.value = ""
        searchQty.value = "1"
        unitPrice = 0.0
        //isGift.value = false
        price_cat_code = "POS"
        clear("cu")
        clear("prod")
    }

    // Button operation for items
    fun onAddItem(){
        if(isValidRow()){
            try {
                createRow { complete ->
                    if(complete){
                        selectedProduct = null
                        searchBarcode.value = ""
                        searchQty.value = "1"
                        giftQty.value = ""
                        //isGift.value = false
                        unitPrice = 0.0
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

    private fun createRow(complete:(Boolean) -> Unit){
        try {
            val strDate = LocalDateTime.now()
            val mItem = tmpSOItems.find { it.sod_prod_Id == selectedProduct!!.pr_Id && it.sod_batch_no == selectedProduct!!.pr_batch_no && it.sod_expiry_date == selectedProduct!!.pr_expiry_date }

            var qty: Double = if(!searchQty.value.isNullOrEmpty()) searchQty.value!!.toDouble() else 0.0

            if(mItem?.sod_pack_qty != null)
                qty += mItem.sod_pack_qty!!

            val gift_qty : Double = (if(!giftQty.value.isNullOrEmpty()) giftQty.value!!.toDouble() else 0.0)
            val ogQty : Double = (if(mItem != null) mItem.sod_gift_qty!!.toDouble() else 0.0)

            //val newQty = qty + if(isGift.value == true) 0.0 else gift_qty
            val newQty = qty + gift_qty

           // val lineTotal = if(isGift.value == true) 0.0 else  unitPrice * newQty
            val lineTotal = unitPrice * newQty
            //val netTotal = if(isGift.value == true) 0.0 else lineTotal
            //val gdisValue = if(isGift.value == true) 0.0 else unitPrice * gift_qty
            val gdisValue =  unitPrice * (gift_qty + ogQty)
            val gdis_per = ((gdisValue / lineTotal) * 100)

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

            val user = App.prefs.saveUser!!

            if(mItem == null){
                rowNo++
                val itemEo = Sale_Order_Items(rowNo, 0, selectedProduct!!.pr_Id, selectedProduct!!.pr_uom_Id, newQty, 1.00, newQty, gift_qty, unitPrice, price_afd,
                    lineTotal, lDisPer, disValue, netTotal, selectedProduct!!.pr_wr_Id,  selectedProduct!!.pr_batch_no,  selectedProduct!!.pr_expiry_date,
                    selectedProduct!!.pr_mfg_date, null, null, null, false,"${strDate}",
                    "${user.id}", "${strDate}", "${user.id}")
                itemEo.sod_prod_name = selectedProduct!!.pr_description_ar
                itemEo.sod_barcode = selectedProduct!!.pr_barcode
                itemEo.sod_partno = selectedProduct!!.pr_partno
                itemEo.sod_pr_is_batch = selectedProduct!!.pr_is_batch_no
                tmpSOItems.add(itemEo)
            }
            else{
                mItem.sod_pack_qty = newQty
                mItem.sod_unit_qty = newQty
                mItem.sod_gift_qty = gift_qty + ogQty

                mItem.sod_line_total = lineTotal
                mItem.sod_net_total = netTotal
                mItem.sod_discount = lDisPer
                mItem.sod_disvalue = disValue
                mItem.updated_at = "$strDate"
            }
            _soItems.value = arrayListOf()
            _soItems.postValue(tmpSOItems)

            complete(true)
        }catch (e: Exception){
            complete(false)
            msgListener?.onFailure("Error during add item in list createRow : ${e.message}")
        }
    }

    private fun isValidRow(): Boolean{
        var isSuccessful = true
        var msg: String? = ""
        var tQty = 0.0
        val mItem = tmpSOItems.find { it.sod_prod_Id == selectedProduct!!.pr_Id && it.sod_batch_no == selectedProduct!!.pr_batch_no && it.sod_expiry_date == selectedProduct!!.pr_expiry_date }
        if(mItem != null){
            tQty +=  mItem.sod_unit_qty!!
        }
        val gQty: Double = (if(!giftQty.value.isNullOrEmpty()) giftQty.value!!.toDouble() else 0.0) +  (if(mItem != null) mItem.sod_gift_qty!!.toDouble() else 0.0)

        val qty = if(!searchQty.value.isNullOrEmpty()) searchQty.value!!.toDouble() else 0.0
        tQty += qty + gQty

        val pr_qty: Double = if(selectedProduct?.pr_qty != null) selectedProduct?.pr_qty!!  else 0.0

        if (selectedProduct == null && searchBarcode.value != "") {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_invalid_product)
        }

        if(disPer.value != null && disPer.value!!.length > 0){
            val tmpDisPer =  disPer.value!!.toDouble()
            val disPerLimit = App.prefs.saveUser!!.dis_Per ?: 0.0

            if(tmpDisPer > disPerLimit) {
                val str: String = ctx!!.resources!!.getString(R.string.msg_error_discount_overflow)
                msg += (if (msg!!.length > 0) "\n\r" else "") + String.format(str, App.prefs.saveUser!!.dis_Per!!)
            }
        }

        if(pr_qty <= 0){
            msg += (if(msg!!.length > 0) "\n\r" else "")  + ctx!!.resources!!.getString(R.string.msg_error_not_available_product_qty)
        }

        if (qty <= 0 && gQty <= 0) {
            msg += (if(msg!!.length > 0) "\n\r" else "")  + ctx!!.resources!!.getString(R.string.msg_error_invalid_qty)
        }

        if(tQty > pr_qty){
            msg += (if(msg!!.length > 0) "\n\r" else "")  + ctx!!.resources!!.getString(R.string.msg_error_not_required_qty_less_product_qty)
        }

        if (unitPrice == 0.00) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_invalid_price)
        }

        if((qty % 1) != 0.0){
            msg += (if(msg!!.length > 0) "\n\r" else "")  + ctx!!.resources!!.getString(R.string.msg_error_qty_has_digit)
        }

        if((gQty % 1) != 0.0){
            msg += (if(msg!!.length > 0) "\n\r" else "")  + ctx!!.resources!!.getString(R.string.msg_error_gift_qty_has_digit)
        }

        if(!msg.isNullOrEmpty()){
            isSuccessful = false
            msgListener?.onFailure(msg)
        }

        return isSuccessful
    }

    fun setTotals(){
        totalAmount.postValue(soItems.value!!.sumByDouble{ it.sod_line_total ?: 0.0 } )
        totalDiscount.postValue(soItems.value!!.sumByDouble { it.sod_disvalue  ?: 0.0 })
        netTotal.postValue(soItems.value!!.sumByDouble { it.sod_net_total ?: 0.0 })
    }

    fun onItemDelete(orderItem: Sale_Order_Items) {
        addNavigator?.onDelete(orderItem)
    }

    fun deleteItem(baseEo: Sale_Order_Items){
        if(baseEo.sod_Id != 0){
            // delete from database
            val item = _soItems.value?.find { it.sod_rowNo == baseEo.sod_rowNo }
            if(item != null){
                tmpDeletedItems.add(item)
            }
        }
        // delete from current list
        tmpSOItems.remove(baseEo)
        _soItems.value = _soItems.value?.filter { it.sod_rowNo != baseEo.sod_rowNo }
    }

    fun clearItems(){
        tmpSOItems.clear()
        _soItems.postValue(tmpSOItems)
    }
    //////////////////////////////////////////////////////////////
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

    fun cancelJob(){
        orderRepository.cancelJob()
        masterDataRepository.cancelJob()
    }
}
