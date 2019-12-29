package com.mawared.mawaredvansale.controller.sales.invoices.addinvoice

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
import com.mawared.mawaredvansale.data.db.entities.md.Currency
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Items
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.invoices.IInvoiceRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.lazyDeferred
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.util.*

class AddInvoiceViewModel(private val saleRepository: IInvoiceRepository,
                          private val masterDataRepository: IMDataRepository) : BaseViewModel() {

    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    private val _wr_id: Int = if(App.prefs.savedSalesman?.sm_warehouse_id != null)  App.prefs.savedSalesman!!.sm_warehouse_id!! else 0
    var mode: String = "Add"
    var msgListener: IMessageListener? = null

    var addNavigator: IAddNavigator<Sale_Items>? = null
    var ctx: Context? = null

    // google map location GPS
    var location: Location? = null

    var _baseEo: MutableLiveData<Sale> = MutableLiveData()
    var allowed_discount: MutableLiveData<Boolean> = MutableLiveData(false)

    private var tmpInvoiceItems: ArrayList<Sale_Items> = arrayListOf()
    private var tmpDeletedItems: ArrayList<Sale_Items> = arrayListOf()

    private val _invoiceItems = MutableLiveData<List<Sale_Items>>()
    val invoiceItems: LiveData<List<Sale_Items>>
        get() = _invoiceItems

    var selectedCustomer: Customer? = null
    var selectedProduct: Product? = null

    private var rowNo: Int = 0
    var docNo = MutableLiveData<String>()
    var docDate = MutableLiveData<String>()
    var totalAmount : MutableLiveData<Double> = MutableLiveData()
    var netTotal: MutableLiveData<Double> = MutableLiveData()
    var totalDiscount: MutableLiveData<Double> = MutableLiveData()

    var searchQty: MutableLiveData<String> = MutableLiveData("1")
    var searchBarcode: MutableLiveData<String> = MutableLiveData()
    var giftQty: MutableLiveData<String> = MutableLiveData("")
    var disPer: MutableLiveData<String> = MutableLiveData("")

    var _entityEo: Sale? = null
    private val sl_id : MutableLiveData<Int> = MutableLiveData()
    val entityEo: LiveData<Sale> = Transformations
        .switchMap(sl_id){
            saleRepository.getInvoice(it)
        }

    val customerList by lazyDeferred { masterDataRepository.getCustomer()  }

    private val _term: MutableLiveData<String> = MutableLiveData()
    val productList: LiveData<List<Product>> = Transformations
        .switchMap(_term){
            masterDataRepository.getProducts(it, App.prefs.savedSalesman?.sm_warehouse_id, "POS")
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

    var discount: Discount? = null
    val mDiscount: LiveData<Discount> = Transformations
        .switchMap(_prod_Id){
            masterDataRepository.getDiscountItem(it, LocalDate.now(), App.prefs.saveUser!!.org_Id)
        }
    var bcCurrency: Currency? = null
    private val _sale_cr_symbole: MutableLiveData<String> = MutableLiveData()
    val saleCurrency: LiveData<Currency> = Transformations
        .switchMap(_sale_cr_symbole){
            masterDataRepository.getCurrencyByCode(it)
        }

    var lcCurrency: Currency? = null
    private val _lc_cr_symbole: MutableLiveData<String> = MutableLiveData()
    val lCurrency: LiveData<Currency> = Transformations
        .switchMap(_lc_cr_symbole){
            masterDataRepository.getCurrencyByCode(it)
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

    fun setLCurrency(cr_code: String){
        if(_lc_cr_symbole.value == cr_code){
            return
        }
        _lc_cr_symbole.value = cr_code
    }

    fun setItems(items: List<Sale_Items>?){
        if(items != null && _invoiceItems == items){
            return
        }
        _invoiceItems.value = items
    }
    //---------------------
    //---- button function
    fun onSave() {
        if (isValid()) {
            try {
                val user = App.prefs.saveUser
                val strDate = LocalDateTime.now()
                val totalAmount: Double = tmpInvoiceItems.sumByDouble { it.sld_line_total!! }
                val totalDiscount: Double = tmpInvoiceItems.sumByDouble { if(it.sld_dis_value == null) 0.00 else it.sld_dis_value!! }
                val netAmount: Double = tmpInvoiceItems.sumByDouble { it.sld_net_total!! }
                val dtFull = docDate.value + " " + LocalTime.now()
                val baseEo = Sale(
                    0, dtFull, "${voucher?.vo_prefix}","", user?.cl_Id, user?.org_Id, mVoucher.value!!.vo_Id,  selectedCustomer?.cu_Id,
                    _sm_id, null, totalAmount, totalDiscount, netAmount, bcCurrency?.cr_id, lcCurrency?.cr_id, rate,false,
                    location?.latitude, location?.longitude,"$strDate", "${user?.id}", "$strDate", "${user?.id}"
                )
                if(mode != "Add"){
                    baseEo.sl_Id = _entityEo!!.sl_Id
                    baseEo.created_at = _entityEo!!.created_at
                    baseEo.created_by = _entityEo!!.created_by
                    baseEo.sl_rate = _entityEo!!.sl_rate
                }
                baseEo.items.addAll(tmpInvoiceItems)

                Coroutines.main {
                    try {
                        val response = saleRepository.SaveOrUpdate(baseEo)
                        if(response.isSuccessful){
                            _baseEo.value = response.data
                        }
                        else{
                            msgListener?.onFailure("Error message when try to save sale invoice. Error is ${response.message}")
                        }
                    }catch (e: Exception){
                        msgListener?.onFailure("Error message when try to save sale invoice. Error is ${e.message}")
                    }
                }
            }catch (e: Exception){
                msgListener?.onFailure("${ctx!!.resources!!.getString(R.string.msg_exception)} Exception is ${e.message}")
            }
        }
    }

    fun setTotals(){
        totalAmount.postValue(invoiceItems.value!!.sumByDouble{ it.sld_line_total ?: 0.00 } )
        totalDiscount.postValue(invoiceItems.value!!.sumByDouble { it.sld_dis_value  ?: 0.00 })
        netTotal.postValue(invoiceItems.value!!.sumByDouble { it.sld_net_total ?: 0.00 })
    }

    private fun isValid(): Boolean {
        var isSuccess = true
        var msg: String? = ""
        if (docDate.value == null) {
            msg =  ctx!!.resources!!.getString(R.string.msg_error_invalid_date)
        }
        if (selectedCustomer == null) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_customer)
        }

        if (tmpInvoiceItems.count() == 0) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_items)
        }

        if (App.prefs.saveUser == null) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_currency)
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
        searchBarcode.value = null
        searchQty.value = "1"

        tmpInvoiceItems.clear()
        clear("cu")
        clear("prod")
    }

    //-------------------------------------------
    //---- row function
    fun onAddItem() {

        if (isValidRow()) {
            try {
                createRow(){ complete ->
                    if(complete){
                        // reset item
                        selectedProduct = null
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

    private fun createRow(complete:(Boolean) -> Unit) {
        try {
            rowNo++
            val strDate = LocalDateTime.now()
            val qty = searchQty.value!!.toDouble()
            var gQty: Double = 0.00
            if(giftQty.value!!.length > 0){
                gQty = giftQty.value!!.toDouble()
            }
            val lineTotal = unitPrice * qty
            var disValue = 0.00
            var lDisPer: Double = 0.00
            if(disPer.value!!.length > 0){
                lDisPer = disPer.value!!.toDouble()
                disValue = (lDisPer / 100) * lineTotal
            }
            if(discount != null){
                if(discount!!.pr_dis_type == "P"){
                    lDisPer = discount!!.pr_dis_value!!
                    disValue = (lDisPer / 100) * lineTotal
                }else{
                    lDisPer = (discount!!.pr_dis_value!! / lineTotal) * 100
                    disValue = discount!!.pr_dis_value!! * qty
                }
            }


            val netTotal = (lineTotal - disValue)
            val user = App.prefs.saveUser
            val mItem = tmpInvoiceItems.find { it.sld_prod_Id == selectedProduct!!.pr_Id }

            if (mItem == null) {
                val item = Sale_Items(0, rowNo, null, selectedProduct!!.pr_Id,
                   selectedProduct!!.pr_uom_Id, qty, 1.00, qty, gQty,
                    unitPrice, lineTotal, lDisPer, disValue, netTotal, null, null, null,
                    _wr_id,selectedProduct!!.pr_batch_no, selectedProduct!!.pr_expiry_date,  selectedProduct!!.pr_mfg_date,
                    "$strDate","${user?.id}", "$strDate", "${user?.id}"
                )
                item.sld_prod_name = selectedProduct!!.pr_description
                item.sld_prod_name_ar = selectedProduct!!.pr_description_ar
                item.sld_barcode =  selectedProduct!!.pr_barcode

                tmpInvoiceItems.add(item)
            } else {
                mItem.sld_pack_qty = mItem.sld_pack_qty!! + qty
                mItem.sld_unit_qty = mItem.sld_pack_qty!! + mItem.sld_pack_size!!
                mItem.sld_line_total = mItem.sld_pack_qty!! * mItem.sld_unit_price!!
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
        val qty = searchQty.value?.toDouble()
        if (selectedProduct == null && searchBarcode.value != "") {
            msg = ctx!!.resources!!.getString(R.string.msg_error_invalid_product)

        }
        if (qty == null || qty <= 0.00) {
            msg += (if(msg!!.length > 0) "\n\r" else "")  + ctx!!.resources!!.getString(R.string.msg_error_invalid_qty)

        }

        if (unitPrice == 0.00) {
            msg += (if(msg!!.length > 0) "\n\r" else "")  + ctx!!.resources!!.getString(R.string.msg_error_invalid_price)
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

    fun deleteItem(baseEo: Sale_Items){
        if(baseEo.sld_Id != 0){
            // delete from database
            val item = _invoiceItems.value?.find { it.sld_rowNo == baseEo.sld_rowNo }
            if(item != null){
                tmpDeletedItems.add(item)
            }
        }
        // delete from current list
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