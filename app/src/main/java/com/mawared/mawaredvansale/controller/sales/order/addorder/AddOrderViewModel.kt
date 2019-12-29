package com.mawared.mawaredvansale.controller.sales.order.addorder

import android.content.Context
import android.content.res.Resources
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
import com.mawared.mawaredvansale.utilities.lazyDeferred
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class AddOrderViewModel(private val orderRepository: IOrderRepository,
                        private val masterDataRepository: IMDataRepository) : BaseViewModel() {

    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    var mode: String = "Add"
    var ctx: Context? = null

    // listener
    var msgListener: IMessageListener? = null
    var addNavigator: IAddNavigator<Sale_Order_Items>? = null

    // google map location GPS
    var location: Location? = null

    var rowNo: Int = 0
    var docNo: MutableLiveData<String> = MutableLiveData()
    var docDate = MutableLiveData<String>()
    var totalAmount : MutableLiveData<Double> = MutableLiveData()
    var netTotal: MutableLiveData<Double> = MutableLiveData()
    var totalDiscount: MutableLiveData<Double> = MutableLiveData()

    var searchQty: MutableLiveData<String> = MutableLiveData("1")
    var searchBarcode: MutableLiveData<String> = MutableLiveData()

    var selectedCustomer: Customer? = null
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
    val customerList by lazyDeferred { masterDataRepository.getCustomer()  }

    private val _term: MutableLiveData<String> = MutableLiveData()
    val productList: LiveData<List<Product>> = Transformations
        .switchMap(_term){
            masterDataRepository.getProductsByUserWarehouse(it, _sm_id, "POS")
        }

    var rate : Double = 0.00
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

    var unitPrice : Double = 0.00
    private val _prod_Id: MutableLiveData<Int> = MutableLiveData()
    val mProductPrice: LiveData<Product_Price_List> = Transformations
        .switchMap(_prod_Id){
            masterDataRepository.getProductPrice(it)
        }

    var bcCurrency: Currency? = null
    private val _sale_cr_symbole: MutableLiveData<String> = MutableLiveData()
    val saleCurrency: LiveData<Currency> = Transformations
        .switchMap(_sale_cr_symbole){
            masterDataRepository.getCurrencyByCode(it)
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

    fun setSaleCurrency(cr_code: String){
        if(_sale_cr_symbole.value == cr_code){
            return
        }
        _sale_cr_symbole.value = cr_code
    }

    fun setItems(items: List<Sale_Order_Items>?){
        if(items != null && _soItems == items){
            return
        }
        _soItems.value = items
    }
    //---------------------------------------
    //----- button function
    fun onSave(){
        if(isValid()){
            try {
                val user = App.prefs.saveUser
                val totalAmount: Double = tmpSOItems.sumByDouble { it.sod_line_total!! }
                val netAmount: Double = tmpSOItems.sumByDouble { it.sod_line_total!! }
                val strDate= LocalDateTime.now()
                val dtFull = docDate.value + " " + LocalTime.now()
                val baseEo = Sale_Order( user?.cl_Id, user?.org_Id,0, dtFull,
                    "", mVoucher.value!!.vo_prefix, mVoucher.value!!.vo_Id,
                    _sm_id, selectedCustomer!!.cu_Id, null, totalAmount, 0.00, netAmount, bcCurrency!!.cr_id, rate,
                    false, location?.latitude, location?.longitude, "$strDate",
                    "${user?.id}", "$strDate", "${user?.id}"
                )

                if(mode != "Add"){
                    baseEo.so_id = _entityEo!!.so_id
                    baseEo.created_at = _entityEo!!.created_at
                    baseEo.created_by = _entityEo!!.created_by
                    baseEo.so_rate = _entityEo!!.so_rate
                }

                baseEo.items.addAll(tmpSOItems)

                Coroutines.main {
                    try {
                        val response = orderRepository.SaveOrUpdate(baseEo)
                        if(response.isSuccessful){
                            _baseEo.value = response.data
                        }
                        else{
                            msgListener?.onFailure("Error message when try to save order. Error is ${response.message}")
                        }
                    }catch (e: Exception){
                        msgListener?.onFailure("Error message when try to save order. Error is ${e.message}")
                    }
                }

            }catch (e: Exception){
                msgListener?.onFailure("${ctx!!.resources!!.getString(R.string.msg_exception)} Exception is ${e.message}")
            }
        }
    }

    private fun isValid(): Boolean{
        var isSuccessful = true
        var msg: String? = ""

        if (docDate.value == null) {
                msg =  ctx!!.resources!!.getString(R.string.msg_error_invalid_date)
        }
        if (selectedCustomer == null) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_customer)
        }

        if (tmpSOItems.count() == 0) {
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

    fun onNew(){
        val strDate: LocalDate = LocalDate.now()
        docNo.value = ""
        docDate.value = returnDateString(strDate.toString())
        totalAmount.value = 0.00
        netTotal.value = 0.00
        totalDiscount.value = 0.00
        clear("cu")
        clear("prod")
    }

    // Button operation for items
    fun onAddItem(){
        if(isValidRow()){
            try {
                createRow() { complete ->
                    if(complete){
                        searchBarcode.value = ""
                        searchQty.value = "1"
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
            rowNo++
            val strDate = LocalDateTime.now()
            val qty = searchQty.value!!.toDouble()
            val lineTotal = unitPrice * qty
            val netTotal = lineTotal
            val mItem = tmpSOItems.find { it.sod_prod_Id == selectedProduct!!.pr_Id }
            val user = App.prefs.saveUser

            if(mItem == null){
                val itemEo = Sale_Order_Items(rowNo, 0, selectedProduct!!.pr_Id, selectedProduct!!.pr_uom_Id, qty, 1.00, qty, unitPrice,
                    lineTotal, null, null, netTotal, selectedProduct!!.pr_wr_Id,  selectedProduct!!.pr_batch_no,  selectedProduct!!.pr_expiry_date,  selectedProduct!!.pr_mfg_date,
                    null, null, null,"${strDate}",
                    "${user?.id}", "${strDate}", "${user?.id}")
                itemEo.sod_prod_name = selectedProduct!!.pr_description_ar
                itemEo.sod_barcode = selectedProduct!!.pr_barcode

                tmpSOItems.add(itemEo)
            }
            else{
                mItem.sod_pack_qty = mItem.sod_pack_qty!! + qty
                mItem.sod_unit_qty = mItem.sod_pack_qty!! + mItem.sod_pack_size!!
                mItem.sod_line_total = mItem.sod_pack_qty!! * mItem.sod_unit_price!!
            }
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
        val qty = searchQty.value?.toDouble()
        if(qty === null || qty == 0.00){
            msg = ctx!!.resources!!.getString(R.string.msg_error_invalid_qty)
        }

        if (selectedProduct == null && searchBarcode.value != "") {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_invalid_product)
        }

        if (unitPrice == 0.00) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_invalid_price)
        }

        if(!msg.isNullOrEmpty()){
            isSuccessful = false
            msgListener?.onFailure(msg)
        }

        return isSuccessful
    }

    fun setTotals(){
        totalAmount.postValue(soItems.value!!.sumByDouble{ it.sod_line_total ?: 0.00 } )
        totalDiscount.postValue(soItems.value!!.sumByDouble { it.sod_disvalue  ?: 0.00 })
        netTotal.postValue(soItems.value!!.sumByDouble { it.sod_net_total ?: 0.00 })
    }

    fun onItemDelete(orderItem: Sale_Order_Items) {
        addNavigator?.onDelete(orderItem)
    }

    fun deleteItem(baseEo: Sale_Order_Items){
        if(baseEo.sod_id != 0){
            // delete from database
            val item = _soItems.value?.find { it.sod_rowNo == baseEo.sod_rowNo }
            if(item != null){
                tmpDeletedItems.add(item)
            }
        }
        // delete from current list
        _soItems.value = _soItems.value?.filter { it.sod_rowNo != baseEo.sod_rowNo }
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
