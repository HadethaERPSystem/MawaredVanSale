package com.mawared.mawaredvansale.controller.transfer.transferentry

import android.content.res.Resources
import android.location.Location
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.data.db.entities.md.Voucher
import com.mawared.mawaredvansale.data.db.entities.md.Warehouse
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer_Items
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.transfer.ITransferRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import java.util.*

class TransferEntryViewModel(private val repository: ITransferRepository, private val masterDataRepository: IMDataRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.saveUser?.id != null)  App.prefs.saveUser!!.id else 0
    private val _wr_id: Int = if(App.prefs.savedSalesman?.sm_warehouse_id != null)  App.prefs.savedSalesman!!.sm_warehouse_id!! else 0
    var mode: String = "Add"
    var msgListener: IMessageListener? = null
    var visible = View.VISIBLE

    var addNavigator: IAddNavigator<Transfer_Items>? = null
    var resources: Resources? = null
    var isRunning: Boolean = false
    // google map location GPS
    var location: Location? = null

    val _baseEo: MutableLiveData<Transfer> = MutableLiveData()

    private var tmpItems: ArrayList<Transfer_Items> = arrayListOf()
    private var tmpDeletedItems: ArrayList<Transfer_Items> = arrayListOf()

    private val _items = MutableLiveData<List<Transfer_Items>>()
    val items: LiveData<List<Transfer_Items>>
        get() = _items


    var selectToWarehouse: Warehouse? = null
    var selectedProduct: Product? = null

    var rowNo: Int = 0
    var docNo = MutableLiveData<String>()
    var docDate = MutableLiveData<String>()

    var searchQty: MutableLiveData<String> = MutableLiveData("1")
    var searchBarcode: MutableLiveData<String> = MutableLiveData()

    var _entityEo: Transfer? = null
    private val tr_id : MutableLiveData<Int> = MutableLiveData()
    val entityEo: LiveData<Transfer> = Transformations
        .switchMap(tr_id){
            repository.getById(it)
        }

    private val _term: MutableLiveData<String> = MutableLiveData()
    val productList: LiveData<List<Product>> = Transformations
        .switchMap(_term){
            masterDataRepository.getProductsBySearch(it)
        }

    val warEoList by lazy {
        masterDataRepository.warehouse_GetAll()
    }

    var voucher: Voucher? = null
    private val _vo_code: MutableLiveData<String> = MutableLiveData()
    val mVoucher: LiveData<Voucher> =Transformations
        .switchMap(_vo_code){
            masterDataRepository.getVoucherByCode(it)
        }

    //------------- set function
    fun setId(id: Int){
        if(tr_id.value == id){
            return
        }
        tr_id.value = id
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

    fun setItems(items: List<Transfer_Items>?){
        if(items != null && _items == items){
            return
        }
        _items.value = items ?: arrayListOf()
        if(items != null)
            tmpItems.addAll(items)
    }

    //---------------------
    //---- button function
    fun onSave() {
        if (isValid()) {
            try {
                isRunning = true
                val user = App.prefs.saveUser
                val strDate = LocalDateTime.now()


                val baseEo = Transfer(
                    user?.cl_Id, user?.org_Id,0, "${docDate.value}", mVoucher.value!!.vo_Id,"${voucher?.vo_prefix}","",
                    App.prefs.savedSalesman?.sm_warehouse_id,null, false,"$strDate", "${user?.id}", "$strDate", "${user?.id}"
                )
                if(mode != "Add"){
                    baseEo.tr_Id = _entityEo!!.tr_Id
                    baseEo.created_at = _entityEo!!.created_at
                    baseEo.created_by = _entityEo!!.created_by

                }
                baseEo.items.addAll(tmpItems)
                if(tmpDeletedItems.count() > 0){
                    baseEo.items_deleted.addAll(tmpDeletedItems)
                }
                Coroutines.main {
                    try {
                        val response = repository.upsert(baseEo)
                        if(response.isSuccessful){
                            _baseEo.value = response.data!!
                            isRunning = false
                        }
                        else{
                            msgListener?.onFailure("Error message when try to save request transfer. Error is ${response.message}")
                            isRunning = false
                        }
                    }catch (e: Exception){
                        msgListener?.onFailure("Error message when try to save request transfer. Error is ${e.message}")
                        isRunning = false
                    }
                }

            }catch (e: Exception){
                msgListener?.onFailure("${resources!!.getString(R.string.msg_exception)} Exception is ${e.message}")
                isRunning = false
            }
        }
    }

    private fun isValid(): Boolean {
        var isSuccess = true
        var msg: String? = ""
        if (docDate.value == null) {
            msg =  resources!!.getString(R.string.msg_error_invalid_date)
        }

        if (selectToWarehouse == null) {
            msg += "\n\r" + resources!!.getString(R.string.msg_error_no_to_warehouse)
        }

        if (tmpItems.count() == 0) {
            msg += "\n\r" +resources!!.getString(R.string.msg_error_no_items)
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
        selectToWarehouse = null
        searchBarcode.value = ""
        searchQty.value = "1"

        tmpItems.clear()
        clear("from_wr")
        clear("to_wr")
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

    private fun createRow(complete:(Boolean) -> Unit) {
        try {

            val strDate = LocalDateTime.now()
            val mItem = tmpItems.find { it.trd_prod_Id == selectedProduct!!.pr_Id }
            val qty = searchQty.value!!.toDouble() + (if(mItem?.trd_pack_qty != null)   mItem.trd_pack_qty!! else 0.00)

            val user = App.prefs.saveUser

            if (mItem == null) {
                rowNo++
                val item = Transfer_Items(0, rowNo, selectedProduct!!.pr_Id,
                    selectedProduct!!.pr_uom_Id, qty, 1.00, qty,
                     "$strDate", "${user?.id}", "$strDate", "${user?.id}"
                )
                item.trd_prod_name = selectedProduct!!.pr_description
                item.trd_prod_name_ar = selectedProduct!!.pr_description_ar
                item.trd_barcode =  selectedProduct!!.pr_barcode

                tmpItems.add(item)
            } else {
                mItem.trd_pack_qty = qty
                mItem.trd_unit_qty = qty
            }
            _items.postValue(tmpItems)
            complete(true)

        }catch (e: Exception){
            complete(false)
            msgListener?.onFailure("${resources!!.getString(R.string.msg_error_add_item)} : ${e.message}")
        }
    }

    private fun isValidRow(): Boolean {
        var isSuccessful = true
        var msg: String? = null
        if (selectedProduct == null && searchBarcode.value != "") {
            msg = resources!!.getString(R.string.msg_error_invalid_product)

        }
        if (searchQty.value.isNullOrEmpty()) {
            msg = "\n\r" + resources!!.getString(R.string.msg_error_invalid_qty)

        }

        if (!msg.isNullOrEmpty()) {
            isSuccessful = false
            msgListener?.onFailure(msg)
        }

        return isSuccessful
    }


    fun onItemDelete(item: Transfer_Items) {
        addNavigator?.onDelete(item)
    }

    fun deleteItem(baseEo: Transfer_Items){
        if(baseEo.trd_Id != 0){
            // delete from database
            val item = _items.value?.find { it.trd_rowNo == baseEo.trd_rowNo }
            if(item != null){
                tmpDeletedItems.add(item)
            }
        }
        // delete from tmp
        tmpItems.remove(baseEo)
        // delete from current list
        _items.value = _items.value?.filter { it.trd_rowNo != baseEo.trd_rowNo }
    }
    //----------------------------------

    fun clear(code: String) {
        when(code) {
            //"to_wr" -> selectToWarehouse = null
            "prod"-> selectedProduct = null

        }
        addNavigator?.clear(code)
    }

    fun onDatePicker(v: View) {
        addNavigator?.onShowDatePicker(v)
    }

    fun getProductName(item: Transfer_Items): String?{
        val lang =  Locale.getDefault().toString()
        when(lang.toLowerCase()){
            "en_us" -> return item.trd_prod_name
            "ar_iq" -> return item.trd_prod_name_ar
            else -> return item.trd_prod_name_ar
        }
    }

    fun cancelJob(){
        masterDataRepository.cancelJob()
        repository.cancelJob()
    }
}
