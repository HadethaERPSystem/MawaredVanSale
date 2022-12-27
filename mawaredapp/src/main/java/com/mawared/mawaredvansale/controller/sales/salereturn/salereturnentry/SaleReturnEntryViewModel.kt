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
import com.mawared.mawaredvansale.data.db.entities.md.Currency_Rate
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.data.db.entities.md.Voucher
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return_Items
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.salereturn.ISaleReturnRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import kotlin.math.exp

class SaleReturnEntryViewModel(private val repository: ISaleReturnRepository, private val masterdataRepository: IMDataRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    private val _wr_id: Int = if(App.prefs.savedSalesman?.sm_warehouse_id != null)  App.prefs.savedSalesman!!.sm_warehouse_id!! else 0
    var mode: String = "Add"
    var msgListener: IMessageListener? = null
    var addNavigator: IAddNavigator<Sale_Return_Items>? = null
    var allowed_select_prod: MutableLiveData<Boolean> = MutableLiveData(false)
    var isRunning: Boolean = false
    var pr_is_batch_no: String? = null
    var visible = View.VISIBLE
    // google map location GPS
    var location: Location? = null
    var resources: Resources? = null

    // View model properties
    var rowNo: Int = 0

    var doc_no: MutableLiveData<String> = MutableLiveData()
    var doc_date = MutableLiveData<String>()
    var doc_expiry = MutableLiveData<String>()
    var doc_unit_price = MutableLiveData<String>()
    var totalAmount : MutableLiveData<Double> = MutableLiveData()
    var netTotal: MutableLiveData<Double> = MutableLiveData()
    var totalDiscount: MutableLiveData<Double> = MutableLiveData()
    var cr_symbol: MutableLiveData<String> = MutableLiveData(App.prefs.saveUser?.ss_cr_code ?: "")
    var sr_discPrcnt: Double = 0.0
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
    var oCu_Id: Int? = null
    var term: MutableLiveData<String> = MutableLiveData()
    val customerList : LiveData<List<Customer>> = Transformations.switchMap(term) { masterdataRepository.getCustomers(_sm_id, it)  }

    val networkState by lazy { repository.networkState }
    // product observable data
    var selectedProduct: Product? = null
    private val _term: MutableLiveData<String> = MutableLiveData()
    val productList: LiveData<List<Product>> = Transformations
        .switchMap(_term){
            masterdataRepository.getProductsBySearch(it)//, App.prefs.savedSalesman?.sm_warehouse_id, price_cat_code)
        }

    var selectedInvoice: Product? = null
    private val _term1: MutableLiveData<String> = MutableLiveData()
    val InvoicesList: LiveData<List<Product>> = Transformations
        .switchMap(_term1){
            masterdataRepository.getProducts_InvoicesByCustomer(selectedCustomer!!.cu_ref_Id!!, selectedProduct!!.pr_Id, it)//, App.prefs.savedSalesman?.sm_warehouse_id, price_cat_code)
        }

    var rate : Double = 0.0
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

    var unitPrice : Double = 0.0
    var price_cat_code = "POS"
    var invQty : Double = 0.0
    var ref_rowNo : Int? = 0
    var ref_Id : Int? = null
    var ref_no : String? = null
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

    fun setInvoices(term: String){
        _term1.value = term
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
        _srItems.value = items ?: arrayListOf()
        if(items != null)
            tmpSRItems.addAll(items)
    }
    //////////// button operation
    fun onSave(){
        if(isValid()){
            try {
                isRunning = true
                val user = App.prefs.saveUser!!
                val totalAmount: Double = tmpSRItems.sumByDouble { it.srd_line_total!! }
                val totalDisc: Double = tmpSRItems.sumByDouble { (it.srd_dis_value ?: 0.0) + (it.srd_add_dis_value ?: 0.0) }
                val netAmount: Double = tmpSRItems.sumByDouble { it.srd_net_total!! }
                val strDate = LocalDateTime.now()
                val dtFull = doc_date.value + " " + LocalTime.now()
                val doc_num = doc_no.value?.toInt() ?: 0
                val cu_Id = selectedCustomer?.cu_ref_Id ?: _entityEo?.sr_customerId
                val cu_price_cat_Id = selectedCustomer?.cu_price_cat_Id ?: _entityEo?.sr_price_cat_Id
                val baseEo = Sale_Return(
                    user.cl_Id, user.org_Id,doc_num, dtFull,
                    "", mVoucher.value!!.vo_prefix, mVoucher.value!!.vo_Id,
                    _sm_id, cu_Id, null, totalAmount,totalDisc, netAmount, sr_discPrcnt, user.ss_cr_Id, user.sf_cr_Id, rate,
                    ref_Id, ref_no, false,0, location?.latitude, location?.longitude, cu_price_cat_Id,"$strDate",
                    "${user.id}", "$strDate", "${user.id}"
                )
                baseEo.sr_price_cat_code = price_cat_code
                if(mode != "Add"){
                    baseEo.sr_Id = _entityEo!!.sr_Id
                    baseEo.created_at = _entityEo!!.created_at
                    baseEo.created_by = _entityEo!!.created_by
                    baseEo.sr_rate = _entityEo!!.sr_rate
                }
                baseEo.items.addAll(tmpSRItems)
                if(tmpDeletedItems.count() > 0){
                    baseEo.items_deleted.addAll(tmpDeletedItems)
                }
                Coroutines.main {
                    try {
                        val response = repository.SaveOrUpdate(baseEo)
                        if(response.isSuccessful){
                            _baseEo.value = response.data!!
                            isRunning = false
                        }
                        else{
                            msgListener?.onFailure("Error message when try to save sale return. Error is ${response.message}")
                            isRunning = false
                        }
                    }catch (e: Exception){
                        msgListener?.onFailure("Error message when try to save sale return. Error is ${e.message}")
                        isRunning = false
                    }
                }

            }catch (e: Exception){
                msgListener?.onFailure("${resources!!.getString(R.string.msg_exception)} Exception is ${e.message}")
                isRunning = false
            }
        }
    }

    private fun isValid(): Boolean{
        var isSuccessful = true
        var msg: String? = ""

        if (doc_date.value == null) {
            msg = resources!!.getString(R.string.msg_error_invalid_date)
        }
        if (selectedCustomer == null && _entityEo == null) {
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
        searchBarcode.value = ""
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
                createRow { complete ->
                    if(complete){
                        selectedProduct = null
                        searchBarcode.value = ""
                        searchQty.value = ""
                        unitPrice = 0.0
                        invQty = 0.0
                        ref_Id = null
                        ref_no = null
                        doc_expiry.value = ""
                        doc_unit_price.value = ""
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
            val strDate = LocalDate.now()
            val mItem = tmpSRItems.find { it.srd_prod_Id == selectedProduct!!.pr_Id && it.srd_ref_rowNo == ref_rowNo && it.srd_Id == ref_Id }//&& it.srd_batch_no == selectedInvoice!!.pr_batch_no && it.srd_expiry_date == selectedInvoice!!.pr_expiry_date }
            val pQty = if(!searchQty.value.isNullOrEmpty()) searchQty.value!!.toDouble() else 0.0 + (if(mItem?.srd_pack_qty != null)   mItem.srd_pack_qty!! else 0.0)
            //unitPrice = if(selectedInvoice != null) selectedInvoice!!.pr_unit_price!! else unitPrice
            var price = unitPrice
            if(selectedInvoice != null) {
                price = selectedInvoice!!.pr_unit_price!!
            }

            val lineTotal = price * pQty
            var qty: Double = pQty // if(selectedInvoice != null) selectedInvoice!!.pr_NumInSale!! else 1.0
            var paksize: Double = 1.0

            var disPer: Double = 0.0
            var disValue : Double = 0.0
            var pr_expiry_date: String? = null
            var pr_mfg_date: String? = null
            var pr_batch_no: String? = null
            var add_disc_value : Double = 0.0
            if(selectedInvoice != null){
                paksize = selectedInvoice!!.pr_NumInSale!!
                qty = pQty * paksize
                disPer = selectedInvoice!!.pr_dis_per ?: 0.0
                disValue = if(disPer > 0.0) lineTotal * (1-(disPer / 100)) else 0.0
                pr_expiry_date = selectedInvoice!!.pr_expiry_date
                pr_mfg_date = selectedInvoice!!.pr_mfg_date
                pr_batch_no = selectedInvoice!!.pr_batch_no
                add_disc_value = (lineTotal - disValue) * (1-(selectedInvoice!!.pr_d_discPrcnt/100))
                if(sr_discPrcnt == 0.0)
                    sr_discPrcnt = selectedInvoice!!.pr_d_discPrcnt
            }
            val netTotal = (lineTotal - disValue -  add_disc_value)

            val user = App.prefs.saveUser

            if(mItem == null){
                rowNo++
                val itemEo = Sale_Return_Items(0, rowNo, selectedProduct!!.pr_Id, selectedInvoice!!.pr_uom_Id, pQty, paksize, qty, price,
                    lineTotal, disPer, disValue, selectedInvoice!!.pr_d_discPrcnt, add_disc_value, netTotal, null, null, null, _wr_id, ref_rowNo, ref_Id, ref_no,
                    pr_batch_no, pr_expiry_date,  pr_mfg_date,"$strDate",
                    "${user?.id}", "$strDate", "${user?.id}")

                itemEo.srd_prod_name = selectedProduct?.pr_description_ar ?: ""
                itemEo.srd_barcode = selectedProduct?.pr_barcode ?: ""

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
//        if(pr_is_batch_no == "Y"){
//            if(doc_expiry.value.isNullOrEmpty()){
//                msg += (if(!msg.isNullOrEmpty()) "\n\r" else "") + "Should enter expire date"
//            }
//            if(selectedInvoice == null){
//                msg += (if(!msg.isNullOrEmpty()) "\n\r" else "") + "Should select batch no"
//            }
//        }


        val qty : Double = if(searchQty.value.isNullOrEmpty()) 0.0 else searchQty.value!!.toDouble()
        if(qty <= 0.0){
            msg += (if (msg!!.length > 0) "\n\r" else "") + resources!!.getString(R.string.msg_error_invalid_qty)
        }
        var tQty = qty
        val mItem =
            tmpSRItems.find { it.srd_prod_Id == selectedProduct!!.pr_Id && it.srd_ref_rowNo == ref_rowNo && it.srd_Id == ref_Id }
        if (mItem != null) {
            tQty += mItem.srd_unit_qty!!
        }

        if(tQty > invQty){
            msg += (if(!msg.isNullOrEmpty()) "\n\r" else "") + resources!!.getString(R.string.msg_return_qty_greater_invqty)
        }

        if (selectedProduct == null && searchBarcode.value != "") {
            msg += (if(!msg.isNullOrEmpty()) "\n\r" else "") + resources!!.getString(R.string.msg_error_invalid_product)
        }

        if (unitPrice == 0.0) {
            msg += (if(!msg.isNullOrEmpty()) "\n\r" else "") + resources!!.getString(R.string.msg_error_invalid_price)
        }

        if(!msg.isNullOrEmpty()){
            isSuccessful = false
            msgListener?.onFailure(msg)
        }

        return isSuccessful
    }

    fun setTotals(){
        totalAmount.postValue(srItems.value!!.sumByDouble{ it.srd_line_total ?: 0.0 } )
        totalDiscount.postValue(srItems.value!!.sumByDouble { it.srd_dis_value  ?: 0.0 })
        netTotal.postValue(srItems.value!!.sumByDouble { it.srd_net_total ?: 0.0 })
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
        tmpSRItems.remove(baseEo)
        _srItems.value = _srItems.value?.filter { it.srd_rowNo != baseEo.srd_rowNo }
    }

    fun clearItems(){
        tmpSRItems.clear()
        _srItems.postValue(tmpSRItems)
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
            "invoices"->{
                selectedInvoice = null
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
