package com.mawared.mawaredvansale.controller.sales.delivery.deliveryentry

import android.content.res.Resources
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.md.*
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery_Items
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.delivery.IDeliveryRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import org.threeten.bp.LocalDateTime

class DeliveryEntryViewModel(private val repository: IDeliveryRepository,
                             private val mdrepository: IMDataRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_id != null)  App.prefs.savedSalesman!!.sm_id else 0
    private val _wr_id: Int = if(App.prefs.savedSalesman?.sm_warehouse_id != null)  App.prefs.savedSalesman!!.sm_warehouse_id!! else 0
    var mode: String = "Add"
    var msgListener: IMessageListener? = null

    var addNavigator: IAddNavigator<Delivery_Items>? = null
    var resources: Resources? = null

    // google map location GPS
    var location: Location? = null

    var dl_doc_date: MutableLiveData<String> = MutableLiveData()
    var dl_refNo: MutableLiveData<String> = MutableLiveData()
    var dl_customer_name: MutableLiveData<String> = MutableLiveData()
    var isDelivered: MutableLiveData<Boolean> = MutableLiveData(false)

    val _baseEo: MutableLiveData<Delivery> = MutableLiveData()
//    val savedEntity: LiveData<Delivery> = Transformations
//        .switchMap(_baseEo){
//            repository.update(it)
//        }


    private val _items = MutableLiveData<List<Delivery_Items>>()
    val items: LiveData<List<Delivery_Items>>
        get() = _items

    var selectedCustomer: Customer? = null
    var selectedProduct: Product? = null

    var searchQty: MutableLiveData<String> = MutableLiveData("1")
    var searchBarcode: MutableLiveData<String> = MutableLiveData()

    var _entityEo: Delivery? = null
    val dl_id : MutableLiveData<Int> = MutableLiveData()
    val entityEo: LiveData<Delivery> = Transformations
        .switchMap(dl_id){
            repository.getById(it)
        }

    fun setItems(items: List<Delivery_Items>?){
        if(items != null && _items == items){
            return
        }
        _items.value = items
    }
    //---------------------
    //---- button function
    fun onSave() {
        if (isValid()) {
            try {
                val user = App.prefs.saveUser
                val strDate = LocalDateTime.now()

                val tQty = items.value?.sumByDouble { it.dld_pack_qty!! }
                val dlQty = items.value?.sumByDouble { it.dld_qty!! }

                _entityEo!!.dl_latitude = location?.latitude
                _entityEo!!.dl_longitude = location?.longitude
               _entityEo!!.dl_isDelivered = if(tQty == dlQty) "Y" else if(dlQty != null) "P" else "N"
               _entityEo!!.dl_Id = _entityEo!!.dl_Id
               _entityEo!!.updated_at = strDate.toString()
               _entityEo!!.updated_by = user?.id.toString()

                Coroutines.main {
                    try {
                        val response = repository.update(_entityEo!!)
                        if(response.isSuccessful){
                            _baseEo.value = response.data
                        }
                        else{
                            msgListener?.onFailure("Error message when try to save delivered invoice. Error is ${response.message}")
                        }
                    }catch (e: Exception){
                        msgListener?.onFailure("Error message when try to save delivered invoice. Error is ${e.message}")
                    }
                }
            }catch (e: Exception){
                msgListener?.onFailure("${resources!!.getString(R.string.msg_exception)} Exception is ${e.message}")
            }
        }
    }

    private fun isValid(): Boolean {
        var isSuccess = true
        var msg: String? = null
        if (isDelivered.value == null || isDelivered.value == false) {
            msg =  resources!!.getString(R.string.msg_error_not_selected_to_delivered)
        }

        if (!msg.isNullOrEmpty()) {
            isSuccess = false
            msgListener?.onFailure(msg)
        }
        return isSuccess
    }

    fun onClickDelivered(){
        val tmpItems : ArrayList<Delivery_Items> = arrayListOf()
        for(d in items.value!!){
            d.dld_isDeliverd = true
            d.dld_qty = d.dld_unit_qty
            tmpItems.add(d)

        }
        isDelivered.value = true
        setItems(null)
        setItems(tmpItems as List<Delivery_Items>)
    }
    //-------------------------------------------
    //---- row function
//    fun onAddItem() {
//
//        if (isValidRow()) {
//            try {
//                createRow(){ complete ->
//                    if(complete){
//                        // reset item
//                        selectedProduct = null
//                        searchBarcode.value = ""
//                        searchQty.value = "1"
//
//                        clear("prod")
//                    }else{
//                        msgListener?.onFailure(resources!!.getString(R.string.msg_error_fail_add_item))
//                    }
//                }
//            }catch (e: Exception){
//                msgListener?.onFailure("${resources!!.getString(R.string.msg_error_add_item)} : ${e.message}")
//            }
//        }
//    }

    fun onBarcode(){

    }
//    private fun createRow(complete:(Boolean) -> Unit) {
//        try {
//            rowNo++
//            val strDate = LocalDateTime.now()
//            val qty = searchQty.value!!.toDouble()
//            val lineTotal = unitPrice * qty
//            val netTotal = lineTotal
//            val user = App.prefs.saveUser
//            val mItem = tmpInvoiceItems.find { it.sld_prod_Id == selectedProduct!!.pr_Id }
//
//            if (mItem == null) {
//                val item = Sale_Items(0, rowNo, null, selectedProduct!!.pr_Id,
//                    selectedProduct!!.pr_uom_Id, qty, 1.00, qty,
//                    unitPrice, lineTotal, 0.00, 0.00, netTotal, null, null, null,
//                    _wr_id,"$strDate", "$user?.id", "$strDate", "$user?.id"
//                )
//                item.sld_prod_name = selectedProduct!!.pr_description_ar
//                item.sld_barcode =  selectedProduct!!.pr_barcode
//
//                tmpInvoiceItems.add(item)
//            } else {
//                mItem.sld_pack_qty = mItem.sld_pack_qty!! + qty
//                mItem.sld_unit_qty = mItem.sld_pack_qty!! + mItem.sld_pack_size!!
//                mItem.sld_line_total = mItem.sld_pack_qty!! * mItem.sld_unit_price!!
//            }
//            _invoiceItems.postValue(tmpInvoiceItems)
//            complete(true)
//
//        }catch (e: Exception){
//            complete(false)
//            msgListener?.onFailure("${resources!!.getString(R.string.msg_error_add_item)} : ${e.message}")
//        }
//    }

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

    fun cancelJob(){
        mdrepository.cancelJob()
        repository.cancelJob()
    }
}
