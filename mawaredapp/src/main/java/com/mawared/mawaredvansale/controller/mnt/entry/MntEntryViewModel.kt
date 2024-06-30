package com.mawared.mawaredvansale.controller.mnt.entry


import android.content.Context
import android.location.Location
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.controller.common.dialog.GenericDialog.showDialog
import com.mawared.mawaredvansale.data.db.entities.dms.Document
import com.mawared.mawaredvansale.data.db.entities.md.*
import com.mawared.mawaredvansale.data.db.entities.mnt.*
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.mnt.IMaintenanceRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.util.*
import kotlin.collections.ArrayList

class MntEntryViewModel(private val mntRepository: IMaintenanceRepository,
                        private val masterDataRepository: IMDataRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    private val user = App.prefs.saveUser!!
    var cr_symbol: MutableLiveData<String> = MutableLiveData(App.prefs.saveUser?.ss_cr_code ?: "")
    var mode: String = "Add"
    var addNavigator: IAddNavigator<Mnts>? = null
    var msgListener: IMessageListener? = null
    var ctx: Context? = null
    var visible = View.VISIBLE
    var isRunning: Boolean = false
    // google map location GPS
    var location: Location? = null

    var _baseEo: MutableLiveData<Mnts> = MutableLiveData()

    var selectedCustomer: Customer? = null
    var selectedDocType: MntType? = null
    var selectedMntStatus: MntStatus? = null
    var selectedPriceList: PriceCategory? = null
    var selectedWarranty: Warranty? = null
    var selectedInvoices: Sale? = null

    var oCu_Id: Int? = null
    var selectedProduct: Product? = null
    var selectedDevice: Product? = null

    private var tmpMntTrans : MutableLiveData<Mnts> = MutableLiveData()



    private var tmpTech: ArrayList<MntTech> = arrayListOf()
    private var tmpServ: ArrayList<MntServ> = arrayListOf()

    // Maintenance prop
    var doc_no: MutableLiveData<String> = MutableLiveData()
    var doc_date: MutableLiveData<String> = MutableLiveData()
    var regMntNo: MutableLiveData<String> = MutableLiveData()
    var contRefNo: MutableLiveData<String> = MutableLiveData()
    var near_zone:MutableLiveData<String> = MutableLiveData()
    var address: MutableLiveData<String> = MutableLiveData()
    var serialNo: MutableLiveData<String> = MutableLiveData()
    var war_date: MutableLiveData<String> = MutableLiveData()


    // Maintenance Trans. prop
    var firstDesc: MutableLiveData<String> = MutableLiveData()
    var finalDesc: MutableLiveData<String> = MutableLiveData()
    var is_free: MutableLiveData<Boolean> = MutableLiveData()
    var is_free_serv: MutableLiveData<Boolean> = MutableLiveData()
    var totalWorkCost: MutableLiveData<Double> = MutableLiveData()
    var totalItemsCost: MutableLiveData<Double> = MutableLiveData()
    var totalCost: MutableLiveData<Double> = MutableLiveData()
    // Maintenance Items
    var entryQty: MutableLiveData<String> = MutableLiveData()
    var unitPrice : Double = 0.0
    var serv_unitPrice: Double = 0.0

    var disPer: MutableLiveData<String> = MutableLiveData("")
    var selectedWhs: Warehouse? = null
    var selectedLoc: Loc? = null
    private var tmpDocLines :ArrayList<Document> = arrayListOf()
    private var _docLines = MutableLiveData<List<Document>>()
    val docLines : LiveData<List<Document>>
        get() = _docLines

    private var tmpSpareParts: ArrayList<MntSpareParts> = arrayListOf()
    private var _spPartsLines = MutableLiveData<List<MntSpareParts>>()
    val spPartsLines: LiveData<List<MntSpareParts>>
        get() = _spPartsLines

    private val _servLines = MutableLiveData<List<MntServ>>()
    val servLines : LiveData<List<MntServ>>
        get() = _servLines

    private val _techLines = MutableLiveData<List<MntTech>>()
    val techLines : LiveData<List<MntTech>>
        get() = _techLines;

    // Maintenance Services
    var entrysQty: MutableLiveData<String> = MutableLiveData()
    var selectedService: Servs? = null
    var price_cat_code = "POS"
    var _entityEo : Mnts? = null

    private val id : MutableLiveData<Int> = MutableLiveData()
    val entityEo: LiveData<Mnts> = Transformations
        .switchMap(id){
            mntRepository.getById(it)
        }

    var cu_Id: Int? = null
    var term: MutableLiveData<String> = MutableLiveData()
    val customerList : LiveData<List<Customer>> = Transformations.switchMap(term) {
        masterDataRepository.getCustomers_ByTerm(_sm_id, it, mntTypeCode)
    }

    var _term: MutableLiveData<String> = MutableLiveData()
    val productList: LiveData<List<Product>> = Transformations
        .switchMap(_term){
            masterDataRepository.getProductsByUserWarehouse(it, App.prefs.saveUser!!.id, price_cat_code)
        }

    var devTerm : MutableLiveData<String> = MutableLiveData()
    val deviceList: LiveData<List<Product>> = Transformations
        .switchMap(devTerm){
            masterDataRepository.getProductsByContract(selectedRegMnt?.cont_Id, it)
        }

    var srvTerm: MutableLiveData<String> = MutableLiveData()
    val servicesList: LiveData<List<Servs>> = Transformations.switchMap(srvTerm){
        masterDataRepository.getServicesByTerm(it, price_cat_code)
    }
    var mntTypeCode: String = ""
    val mntTypeList: LiveData<List<MntType>> by lazy {
        masterDataRepository.getMntType()
    }

    val mntStatusList: LiveData<List<MntStatus>> by lazy {
        masterDataRepository.getMntStatus()
    }

    val priceCateList : LiveData<List<PriceCategory>> by lazy{
        masterDataRepository.priceCat_GetAll()
    }

    var invTerm: MutableLiveData<String> = MutableLiveData()
    val mntInvoicesList : LiveData<List<Sale>> = Transformations.switchMap(invTerm){
        masterDataRepository.getInvoicsByTerm(cu_Id, it)
    }

    var selectedRegMnt: RegMnt? = null
    var regTerm: MutableLiveData<String> = MutableLiveData()
    val mntRegList: LiveData<List<RegMnt>> = Transformations.switchMap(regTerm){
        masterDataRepository.getWaiting_RegMnt(it)
    }

    var warTerm: MutableLiveData<String> = MutableLiveData()
    val mntWarrantyList : LiveData<List<Warranty>> = Transformations.switchMap(warTerm){
        masterDataRepository.getWarrantyByTerm(cu_Id, it)
    }

    val networkState: LiveData<NetworkState> by lazy {
        mntRepository.networkState
    }

    val WhsList : LiveData<List<Warehouse>> by lazy { masterDataRepository.warehouse_GetAll() }

    var _whsId : MutableLiveData<Int> = MutableLiveData()
    val locationList : LiveData<List<Loc>> = Transformations.switchMap(_whsId){
        masterDataRepository.location_GetByWhs(it)
    }

    private val _prod_Id: MutableLiveData<Int> = MutableLiveData()

    var discount: Discount? = null
    val mDiscount: LiveData<Discount> = Transformations
        .switchMap(_prod_Id){
            masterDataRepository.getDiscountItem(it, LocalDate.now(), App.prefs.saveUser!!.org_Id, price_cat_code)
        }

    var voucher: Voucher? = null
    var vo_code: MutableLiveData<String> = MutableLiveData()
    val mVoucher: LiveData<Voucher> =Transformations
        .switchMap(vo_code){
            masterDataRepository.getVoucherByCode(it)
        }

    fun setSpPartLines(lines: List<MntSpareParts>){
        _spPartsLines.value = lines
        tmpSpareParts.addAll(lines)
    }

    fun setServLines(lines: List<MntServ>){
        _servLines.value = lines
        tmpServ.addAll(lines)
    }

    fun setProductId(prod_Id: Int){
        _prod_Id.value = prod_Id
    }

    fun setTechLines(lines: List<MntTech>){
        _techLines.value = lines
        tmpTech.addAll(lines)
    }


    // Save function
    fun onSave(Success:(() -> Unit) = {}, Fail:(() -> Unit) = {}) {
        if (isValid()){
            try{
                isRunning = true
                val user = App.prefs.saveUser!!
                val strDate = LocalDateTime.now()
                val dtFull = doc_date.value + " " + LocalTime.now()
                val docnum = doc_no.value?.toInt() ?: 0
                var warno: String? = null
                var warId: Int? = null
                var refno: String? = null
                var refId: Int? = null
                var regId: Int? = null
                var mntNo: Int? = null
                var contId: Int? = null
                if(selectedInvoices != null){
                    refId = selectedInvoices!!.sl_Id
                    refno = selectedInvoices!!.sl_refNo
                }
                if(selectedWarranty != null){
                    warId = selectedWarranty!!.war_Id
                    warno = selectedWarranty!!.war_No
                }

                if(selectedRegMnt != null){
                    regId = selectedRegMnt!!.regMntId
                    mntNo = selectedRegMnt!!.regMntNo
                    contId = selectedRegMnt!!.cont_Id
                }

                // Set data to Maintenance object
                val baseEo = Mnts(docnum, "${dtFull}", "${voucher?.vo_prefix}", "", user.cl_Id, user.org_Id, voucher!!.vo_Id, selectedDocType!!.Id,
                                  "${refno}" ,refId, selectedCustomer!!.cu_ref_Id, selectedDevice!!.pr_Id, "${serialNo.value}",warId ,"${warno}", war_date.value,
                                   near_zone.value, "", regId, mntNo, contId, contRefNo.value, "$strDate", "${user.id}", "$strDate", "${user.id}")
                // Set Maintenance Transaction Object
                baseEo.docMntTrans = MntTrans(user.cl_Id, user.org_Id, 0, 0, selectedCustomer!!.cu_ref_Id, firstDesc.value, finalDesc.value, totalWorkCost.value, totalItemsCost.value,
                                              totalCost.value, selectedDocType!!.Id, selectedMntStatus!!.Id, is_free.value, is_free_serv.value, selectedPriceList!!.prc_Id, selectedDevice!!.pr_uom_Id,
                                             "$strDate", "${user.id}", "$strDate", "${user.id}")
                if(tmpServ.count() > 0) {
                    baseEo.ServLines.addAll(tmpServ)
                }
                if(tmpSpareParts.count() > 0) {
                    baseEo.SpPartsLines.addAll(tmpSpareParts)
                }
                if(tmpTech.count() == 0) {
                    tmpTech.add(MntTech(user.cl_Id, user.org_Id,0, user.empId, "",  "",
                            "$strDate", "${user.id}", "$strDate", "${user.id}" )
                            )
                    baseEo.TechLines.addAll(tmpTech)
                }
                if(tmpDocLines.count() > 0){
                    for(doc in tmpDocLines)
                        baseEo.DocLines.add(Document(doc.fileName, doc.masterType, doc.base64String, null, doc.isNew))
                }
                // Send to API
                Coroutines.main {
                    try {
                        val response = mntRepository.SaveOrUpdate(baseEo)
                        if(response.isSuccessful){
                            _baseEo.value = response.data
                            Success()
                        }else{
                            msgListener?.onFailure("Error message when try to save Maintenance. Error is ${response.message}")
                            Fail()
                        }
                    }catch (e: Exception){
                        msgListener?.onFailure("Error message when try to save Maintenance. Error is ${e.message}")
                        Fail()
                    }
                }
            }catch (e: Exception){
                msgListener?.onFailure("${ctx!!.resources!!.getString(R.string.msg_exception)} Exception is ${e.message}")
                Fail()
            }
        }
    }

    fun setTotals(){
        if(tmpSpareParts.count() > 0)
            totalItemsCost.value = tmpSpareParts.sumByDouble { it.amount!! }
        if(tmpServ.count() > 0)
            totalWorkCost.value = tmpServ.sumByDouble { it.amount!! }

        totalCost.value = (totalItemsCost.value ?: 0.0) + (totalWorkCost.value ?: 0.0)
    }

    private fun isValid(): Boolean{
        var isSuccess = true;
        var msg: String? = ""
        if(selectedCustomer == null){
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_customer)
        }

        if (App.prefs.saveUser == null) {
            msg += (if(msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_no_currency)
        }
        if (!msg.isNullOrEmpty()) {
            isSuccess = false
            msgListener?.onFailure(msg)
        }
        return isSuccess;
    }
    //================================================================================================
    //================= Add Spear part item
    //================================================================================================
    fun onAddItem(){
        if (isValidRow()) {
            try {
                createSpPartRow { complete ->
                    if(complete){
                        // reset item
                        selectedProduct = null
                        selectedWhs = null
                        selectedLoc = null
                        entryQty.value = "1"
                        //giftQty.value = ""
                        disPer.value = ""
                        //isGift.value = false
                        unitPrice = 0.0
                        setTotals()
                        clear("prod")
                        clear("whs")
                        clear("loc")
                    }else{
                        msgListener?.onFailure(ctx!!.resources!!.getString(R.string.msg_error_fail_add_item))
                    }
                }
            }catch (e: Exception){
                msgListener?.onFailure("${ctx!!.resources!!.getString(R.string.msg_error_add_item)} : ${e.message}")
            }
        }
    }

    private fun createSpPartRow(complete:(Boolean) -> Unit) {
        try {

            val mItem = tmpSpareParts.find { it.prod_id == selectedProduct!!.pr_Id && it.batch_no == selectedProduct!!.pr_batch_no && it.expiry_date == selectedProduct!!.pr_expiry_date }

            val strDate = LocalDateTime.now()
            var qty : Double = if(!entryQty.value.isNullOrEmpty()) entryQty.value!!.toDouble() else 0.0
            if(mItem?.qty != null)
                qty += mItem.qty!!

            val newQty = qty + 0
            //val lineTotal = if(isGift.value == true) 0.0 else unitPrice * qty
            var lineTotal = unitPrice * newQty


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

            //val netTotal = if(isGift.value == true) 0.0 else (lineTotal - disValue)
            var netTotal = (lineTotal - disValue)
            //val price_afd = (lDisPer / 100) * unitPrice

            if(is_free.value == true){
                lineTotal = 0.0
                netTotal = 0.0
            }
            val user = App.prefs.saveUser

            if (mItem == null) {

                val item = MntSpareParts(0, 0, null, false,
                    selectedWhs!!.wr_Id, selectedLoc!!.loc_Id, selectedProduct!!.pr_Id,unitPrice, lDisPer, disValue, selectedProduct!!.pr_uom_Id, 1.0,
                    newQty, newQty, netTotal, lineTotal, netTotal, is_free.value, selectedProduct!!.pr_batch_no, selectedProduct!!.pr_expiry_date,
                    "$strDate","${user?.id}", "$strDate", "${user?.id}"
                )
                item.prod_name = selectedProduct!!.pr_description
                item.prod_name_ar = selectedProduct!!.pr_description_ar
                item.barcode =  selectedProduct!!.pr_barcode

                tmpSpareParts.add(item)
            } else {
                mItem.pqty = newQty
                mItem.qty = newQty
                mItem.amount = netTotal
                mItem.line_total = lineTotal
                mItem.net_total = netTotal
                mItem.disc_prcnt = lDisPer
                mItem.dis_value = disValue
                mItem.updated_at = "$strDate"
            }
            _spPartsLines.postValue(tmpSpareParts)
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
        }
        else {
            val mItem =
                tmpSpareParts.find { it.prod_id == selectedProduct!!.pr_Id && it.batch_no == selectedProduct!!.pr_batch_no && it.expiry_date == selectedProduct!!.pr_expiry_date }
            if (mItem != null) {
                tQty += mItem.qty!!
            }

            val gQty: Double =
                0.0 // (if(!giftQty.value.isNullOrEmpty()) giftQty.value!!.toDouble() else 0.0) +  (if(mItem != null) mItem.sld_gift_qty!!.toDouble() else 0.0)
            val qty: Double =
                if (!entryQty.value.isNullOrEmpty()) entryQty.value!!.toDouble() else 0.0

            tQty += qty + gQty

            val pr_qty: Int =
                if (selectedProduct?.pr_qty != null) selectedProduct?.pr_qty!!.toInt() else 0

            if (disPer.value != null && disPer.value!!.length > 0) {
                val tmpDisPer = disPer.value!!.toDouble()
                val disPerLimit = App.prefs.saveUser!!.iDiscPrcnt ?: 0.0

                if (tmpDisPer > disPerLimit) {
                    val str: String =
                        ctx!!.resources!!.getString(R.string.msg_error_discount_overflow)
                    msg += (if (msg!!.length > 0) "\n\r" else "") + String.format(
                        str,
                        App.prefs.saveUser!!.iDiscPrcnt!!
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
    //================================================================================================
    //================= Add Spear part item
    //================================================================================================
    fun onAddService(){
        if (isValidServiceRow()) {
            try {
                createServiceRow { complete ->
                    if(complete){
                        // reset item
                        selectedService = null
                        entrysQty.value = "1"

                        serv_unitPrice = 0.0
                        setTotals()
                        clear("srv")
                    }else{
                        msgListener?.onFailure(ctx!!.resources!!.getString(R.string.msg_error_fail_add_item))
                    }
                }
            }catch (e: Exception){
                msgListener?.onFailure("${ctx!!.resources!!.getString(R.string.msg_error_add_item)} : ${e.message}")
            }
        }
    }

    private fun createServiceRow(complete:(Boolean) -> Unit) {
        try {

            val mItem = tmpServ.find { it.serv_id == selectedService!!.id}

            val strDate = LocalDateTime.now()
            var qty : Double = if(!entrysQty.value.isNullOrEmpty()) entrysQty.value!!.toDouble() else 0.0
            if(mItem?.qty != null)
                qty += mItem.qty!!

            val newQty = qty + 0


            var netTotal = serv_unitPrice * newQty


            if(is_free_serv.value == true){
                netTotal = 0.0
            }
            val user = App.prefs.saveUser

            if (mItem == null) {

                val item = MntServ(0, 0, null, false,
                    selectedService!!.id, selectedService!!.uom_Id, serv_unitPrice, newQty, netTotal, false, is_free_serv.value,
                    "$strDate","${user?.id}", "$strDate", "${user?.id}"
                )
                item.serv_name = selectedService!!.name

                tmpServ.add(item)
            } else {
                mItem.qty = newQty
                mItem.amount = netTotal

                mItem.updated_at = "$strDate"
            }
            _servLines.postValue(tmpServ)
            complete(true)

        }catch (e: Exception){
            complete(false)
            msgListener?.onFailure("${ctx!!.resources!!.getString(R.string.msg_error_add_item)} : ${e.message}")
        }
    }

    fun isValidServiceRow(): Boolean{
        var isSuccessful = true
        var msg: String? = ""
        var tQty = 0.0
        if(selectedService == null){
            msg = ctx!!.resources!!.getString(R.string.msg_error_invalid_service)
        }else {
            val mItem = tmpServ.find { it.serv_id == selectedService!!.id }
            if (mItem != null) {
                tQty += mItem.qty!!
            }
            val qty: Double = if (!entrysQty.value.isNullOrEmpty()) entrysQty.value!!.toDouble() else 0.0

            if (qty <= 0) {
                msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_invalid_qty)
            }

            if (serv_unitPrice == 0.00) {
                msg += (if (msg!!.length > 0) "\n\r" else "") + ctx!!.resources!!.getString(R.string.msg_error_invalid_price)
            }
        }
        if (!msg.isNullOrEmpty()) {
            isSuccessful = false
            msgListener?.onFailure(msg)
        }

        return isSuccessful
    }

    //================================================================================================
    //================= Add document line
    //================================================================================================
    fun addDocument(doc: Document){
        tmpDocLines.add(doc)
        _docLines.postValue(tmpDocLines)
    }
    //================================================================================================
    fun onDatePicker(v: View) {
        addNavigator?.onShowDatePicker(v)
    }


    fun getProductName(item: MntSpareParts): String?{
        val lang =  Locale.getDefault().toString()
        when(lang.toLowerCase()){
            "en_us" -> return item.prod_name
            "ar_iq" -> return item.prod_name_ar
            else -> return item.prod_name_ar
        }
    }

    fun onItemDelete(baseEo: MntSpareParts) {
        showDialog(ctx!!, ctx!!.resources.getString(R.string.delete_dialog_title), ctx!!.resources.getString(R.string.msg_confirm_delete), baseEo,{
            deleteItem(it)
        })
    }

    fun onServiceDelete(baseEo: MntServ) {
        showDialog(ctx!!, ctx!!.resources.getString(R.string.delete_dialog_title), ctx!!.resources.getString(R.string.msg_confirm_delete), baseEo,{
            deleteService(it)
        })
    }

    fun deleteItem(baseEo: MntSpareParts){
//        if(baseEo.sld_Id != 0){
//            // delete from database
//            val item = _invoiceItems.value?.find { it.sld_rowNo == baseEo.sld_rowNo }
//            if(item != null){
//                tmpDeletedItems.add(item)
//            }
//        }
//        // delete from current list
//        tmpInvoiceItems.remove(baseEo)
//        _invoiceItems.value = _invoiceItems.value?.filter { it.sld_rowNo != baseEo.sld_rowNo }
    }

    fun deleteService(baseEo: MntServ){
//        if(baseEo.sld_Id != 0){
//            // delete from database
//            val item = _invoiceItems.value?.find { it.sld_rowNo == baseEo.sld_rowNo }
//            if(item != null){
//                tmpDeletedItems.add(item)
//            }
//        }
//        // delete from current list
//        tmpInvoiceItems.remove(baseEo)
//        _invoiceItems.value = _invoiceItems.value?.filter { it.sld_rowNo != baseEo.sld_rowNo }
    }

    fun clear(code: String) {
        addNavigator?.clear(code)
    }

    fun cancelJob(){
        masterDataRepository.cancelJob()
        mntRepository.cancelJob()
    }
}