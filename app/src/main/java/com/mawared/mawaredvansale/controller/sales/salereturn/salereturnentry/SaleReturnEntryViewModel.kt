package com.mawared.mawaredvansale.controller.sales.salereturn.salereturnentry

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
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return_Items
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.salereturn.ISaleReturnRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.lazyDeferred
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class SaleReturnEntryViewModel(private val repository: ISaleReturnRepository, private val masterdataRepository: IMDataRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    private val _wr_id: Int = if(App.prefs.savedSalesman?.sm_warehouse_id != null)  App.prefs.savedSalesman!!.sm_warehouse_id!! else 0
    var mode: String = "Add"
    var msgListener: IMessageListener? = null
    var addNavigator: IAddNavigator<Sale_Return_Items>? = null
    var allowed_select_prod: MutableLiveData<Boolean> = MutableLiveData(false)
    // google map location GPS
    var location: Location? = null
    var resources: Resources? = null

    // View model properties
    private var rowNo: Int = 0

    var doc_no: MutableLiveData<String> = MutableLiveData()
    var doc_date = MutableLiveData<String>()
    var totalAmount : MutableLiveData<Double> = MutableLiveData()
    var netTotal: MutableLiveData<Double> = MutableLiveData()
    var totalDiscount: MutableLiveData<Double> = MutableLiveData()
    var cr_symbol: MutableLiveData<String> = MutableLiveData(App.prefs.saveUser?.sl_cr_code ?: "")

     var tmpSRItems: ArrayList<Sale_Return_Items> = arrayListOf()
    var tmpDeletedItems: ArrayList<Sale_Return_Items> = arrayListOf()

    var searchQty: MutableLiveData<String> = MutableLiveData("1")
    var searchBarcode: MutableLiveData<String> = MutableLiveData()

    var _entityEo: Sale_Return? = null
    private val _id : MutableLiveData<Int> = MutableLiveData()
    val entityEo: LiveData<Sale_Return> = Transformations
        .switchMap(_id){
            repository.getReturnById(it)
        }

    // for save entity
    val _baseEo: MutableLiveData<Sale_Return> = MutableLiveData()


    // for items
    private val _srItems = MutableLiveData<List<Sale_Return_Items>>()
    val srItems: LiveData<List<Sale_Return_Items>>
        get() = _srItems

    // customer observable data
    var selectedCustomer: Customer? = null
    val customerList by lazyDeferred { masterdataRepository.getCustomers(_sm_id)  }

    // product observable data
    var selectedProduct: Product? = null
    private val _term: MutableLiveData<String> = MutableLiveData()
    val productList: LiveData<List<Product>> = Transformations
        .switchMap(_term){
            masterdataRepository.getProducts(it, App.prefs.savedSalesman?.sm_warehouse_id, price_cat_code)
        }


    var rate : Double = 0.00
    private val _cr_Id: MutableLiveData<Int> = MutableLiveData()
    val currencyRate: LiveData<Currency_Rate> = Transformations
        .switchMap(_cr_Id) {
            masterdataRepository.getRate(it)
        }

    var voucher: Voucher? = null
    private val _vo_code: MutableLiveData<String> = MutableLiveData()
    val mVoucher: LiveData<Voucher> = Transformations
        .switchMap(_vo_code){
            masterdataRepository.getVoucherByCode(it)
        }

    var unitPrice : Double = 0.00
    var price_cat_code = "POS"

    // function to set value
    fun setId(id: Int){
        if(_id.value == id){
            return
        }
        _id.value = id
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

    fun setPriceCategory(){
        price_cat_code = if(selectedCustomer != null && selectedCustomer?.cu_price_cat_code != null) selectedCustomer!!.cu_price_cat_code!! else "POS"
    }

    fun setItems(items: List<Sale_Return_Items>?){
        if(items != null && items == _srItems){
            return
        }
        _srItems.value = items
    }
    //////////// button operation
    fun onSave(){
        if(isValid()){
            try {
                val user = App.prefs.saveUser
                val totalAmount: Double = tmpSRItems.sumByDouble { it.srd_line_total!! }
                val netAmount: Double = tmpSRItems.sumByDouble { it.srd_line_total!! }
                val strDate = LocalDateTime.now()
                val dtFull = doc_date.value + " " + LocalTime.now()
                val baseEo = Sale_Return(
                    user?.cl_Id, user?.org_Id,0, dtFull,
                    "", mVoucher.value!!.vo_prefix, mVoucher.value!!.vo_Id,
                    _sm_id, selectedCustomer!!.cu_ref_Id, null, totalAmount, netAmount, user?.sl_cr_Id, user?.cr_Id, rate,
                    null, false,0, location?.latitude, location?.longitude, selectedCustomer!!.cu_price_cat_Id,"$strDate",
                    "${user?.id}", "$strDate", "${user?.id}"
                )
                baseEo.sr_price_cat_code = price_cat_code
                if(mode != "Add"){
                    baseEo.sr_Id = _entityEo!!.sr_Id
                    baseEo.created_at = _entityEo!!.created_at
                    baseEo.created_by = _entityEo!!.created_by
                    baseEo.sr_rate = _entityEo!!.sr_rate
                }
                baseEo.items.addAll(tmpSRItems)

                Coroutines.main {
                    try {
                        val response = repository.SaveOrUpdate(baseEo)
                        if(response.isSuccessful){
                            _baseEo.value = response.data
                        }
                        else{
                            msgListener?.onFailure("Error message when try to save sale return. Error is ${response.message}")
                        }
                    }catch (e: Exception){
                        msgListener?.onFailure("Error message when try to save sale return. Error is ${e.message}")
                    }
                }

            }catch (e: Exception){
                msgListener?.onFailure("${resources!!.getString(R.string.msg_exception)} Exception is ${e.message}")
            }
        }
    }

    private fun isValid(): Boolean{
        var isSuccessful = true
        var msg: String? = ""

        if (doc_date.value == null) {
            msg = resources!!.getString(R.string.msg_error_invalid_date)
        }
        if (selectedCustomer == null) {
            msg += (if(msg!!.length > 0) "\n\r" else "")  +  resources!!.getString(R.string.msg_error_no_customer)
        }

        if (tmpSRItems.count() == 0) {
            msg += (if(msg!!.length > 0) "\n\r" else "")  + resources!!.getString(R.string.msg_error_no_items)
        }

        if (App.prefs.saveUser == null) {
            msg += (if(msg!!.length > 0) "\n\r" else "")  +  resources!!.getString(R.string.msg_error_no_currency)
        }

        if (!msg.isNullOrEmpty()) {
            isSuccessful = false
            msgListener?.onFailure(msg)
        }
        return isSuccessful
    }

    fun onNew(){
        doc_no.value = ""
        doc_date.value = "${LocalDate.now()}"
        tmpSRItems.clear()
        rowNo = 0
        searchBarcode.value = null
        searchQty.value = "1"
        unitPrice = 0.00
        price_cat_code = "POS"
        clear("cu")
        clear("prod")
    }

    // Add new item to item list
    fun onAddItem(){
        if(isValidRow()){
            try {
                createRow() { complete ->
                    if(complete){
                        selectedProduct = null
                        searchBarcode.value = ""
                        searchQty.value = ""
                        unitPrice = 0.00
                        clear("prod")
                    }else{
                        msgListener?.onFailure(resources!!.getString(R.string.msg_error_fail_add_item))
                    }
                }
            }catch (e: Exception){
                msgListener?.onFailure("${resources!!.getString(R.string.msg_error_add_item)} : ${e.message}")
            }
        }
    }

    private fun createRow(complete:(Boolean) -> Unit){

        try {
            rowNo++
            val strDate = LocalDate.now()
            val mItem = tmpSRItems.find { it.srd_prod_Id == selectedProduct!!.pr_Id }
            val qty = searchQty.value!!.toDouble() + (if(mItem?.srd_pack_qty != null)   mItem.srd_pack_qty!! else 0.00)
            val lineTotal = unitPrice * qty
            val netTotal = lineTotal
            val user = App.prefs.saveUser

            if(mItem == null){
                val itemEo = Sale_Return_Items(0, rowNo, selectedProduct!!.pr_Id, selectedProduct!!.pr_uom_Id, qty, 1.00, qty, unitPrice,
                    lineTotal, 0.00, 0.00, netTotal, null, null, null, _wr_id,null,
                    selectedProduct!!.pr_batch_no, selectedProduct!!.pr_expiry_date,  selectedProduct!!.pr_mfg_date,"$strDate",
                    "${user?.id}", "$strDate", "${user?.id}")

                itemEo.srd_prod_name = selectedProduct!!.pr_description_ar
                itemEo.srd_barcode = selectedProduct!!.pr_barcode

                tmpSRItems.add(itemEo)
            }
            else{
                mItem.srd_pack_qty = qty
                mItem.srd_unit_qty = qty
                mItem.srd_line_total = lineTotal
                mItem.srd_net_total = netTotal
            }
            _srItems.postValue(tmpSRItems)

            complete(true)
        }catch (e: Exception){
            complete(false)
            msgListener?.onFailure("${resources!!.getString(R.string.msg_error_add_item)} : ${e.message}")
        }
    }

    private fun isValidRow(): Boolean{
        var isSuccessful = true
        var msg: String? = ""

        if(searchQty.value.isNullOrEmpty()){
            msg = resources!!.getString(R.string.msg_error_invalid_qty)
        }else if(!(searchQty.value!!matches("-?\\d+(\\.\\d+)?".toRegex()))){
            msg = resources!!.getString(R.string.msg_error_invalid_qty)
        }

        if (selectedProduct == null && searchBarcode.value != "") {
            msg += (if(msg!!.length > 0) "\n\r" else "") + resources!!.getString(R.string.msg_error_invalid_product)
        }

        if (unitPrice == 0.00) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + resources!!.getString(R.string.msg_error_invalid_price)
        }

        if(!msg.isNullOrEmpty()){
            isSuccessful = false
            msgListener?.onFailure(msg)
        }

        return isSuccessful
    }

    fun setTotals(){
        totalAmount.postValue(srItems.value!!.sumByDouble{ it.srd_line_total ?: 0.00 } )
        totalDiscount.postValue(srItems.value!!.sumByDouble { it.srd_dis_value  ?: 0.00 })
        netTotal.postValue(srItems.value!!.sumByDouble { it.srd_net_total ?: 0.00 })
    }

    fun onItemDelete(item: Sale_Return_Items) {
        addNavigator?.onDelete(item)
    }

    fun deleteItem(baseEo: Sale_Return_Items){
        if(baseEo.srd_Id != 0){
            // delete from database
            val item = _srItems.value?.find { it.srd_rowNo == baseEo.srd_rowNo }
            if(item != null){
                tmpDeletedItems.add(item)
            }
        }
        // delete from current list
        _srItems.value = _srItems.value?.filter { it.srd_rowNo != baseEo.srd_rowNo }
    }

    ///////////////////////////////
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
       masterdataRepository.cancelJob()
       repository.cancelJob()
    }
}
