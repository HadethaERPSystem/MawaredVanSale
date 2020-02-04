package com.mawared.mawaredvansale.controller.inventory.stockin.addstockin

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin_Items
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.data.db.entities.md.Voucher
import com.mawared.mawaredvansale.interfaces.IDatePicker
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.interfaces.IResetNavigator
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.stockin.IStockInRepository
import com.mawared.mawaredvansale.utilities.lazyDeferred

class AddStockInViewModel(private val repository: IStockInRepository,private val  masterDataRepository: IMDataRepository) : ViewModel() {
    var msgListener: IMessageListener? = null
    var showDatePicker: IDatePicker? = null
    var addNavigator: IResetNavigator? = null

    private val _w_Id: Int = if(App.prefs.savedSalesman?.sm_warehouse_id != null)  App.prefs.savedSalesman!!.sm_warehouse_id!! else 0

    // define variable for interface
    var docNo: MutableLiveData<Int> = MutableLiveData()
    var docDate: MutableLiveData<String> = MutableLiveData()
    var refNo: MutableLiveData<String> = MutableLiveData()
    var searchLocationName: MutableLiveData<String> = MutableLiveData()
    var searchBarcode: MutableLiveData<String> = MutableLiveData()
    var searchQty: MutableLiveData<String> = MutableLiveData()


    private val _Items = MutableLiveData<List<Stockin_Items>>()
    val items: LiveData<List<Stockin_Items>>
        get() = _Items

    var selectedCustomer: Customer? = null

    val customerList by lazyDeferred {
        masterDataRepository.getCustomers(0)
    }
    // if search on item and select one from list store in this variable
    var selectedProduct: Product? = null

    // product list
    private val _term: MutableLiveData<String> = MutableLiveData()
    val productList: LiveData<List<Product>> = Transformations
        .switchMap(_term){
            masterDataRepository.getProducts(it, App.prefs.savedSalesman?.sm_warehouse_id, "POS")
        }

    // voucher
    var voucher: Voucher? = null
    private val _vo_code: MutableLiveData<String> = MutableLiveData()
    val mVoucher: LiveData<Voucher> =Transformations
        .switchMap(_vo_code){
            masterDataRepository.getVoucherByCode(it)
        }

    // using if search in list of products
    fun setTerm(term: String){
        val update = term
        if(_term.value == update){
            return
        }
        _term.value = update
    }

    // using if search in vouchers and get one record
    fun setVoucherCode(vo_code: String){
        val update = vo_code
        if(_vo_code.value == update){
            return
        }
        _vo_code.value = update
    }

    fun onAddItem(){

    }

    fun onDelete(baseEo: Stockin) {

    }

    fun onItemDelete(entityEo: Stockin_Items) {
//        if(entityEo.sin_Id != 0){
//
//        }
    }

    fun clear(code: String) {
        when(code){
            "cu" -> selectedCustomer = null
            "prod" -> selectedProduct = null
        }
        addNavigator?.clear(code)
    }


    fun onClearInvStatus(){

    }

    fun onClearRetRefNo(){

    }

    fun onDatePicker(v: View) {
        showDatePicker?.ShowDatePicker(v)
    }

    fun setDatePicker(datePicker: IDatePicker) {
        showDatePicker = datePicker
    }

    fun setNavigator(navigator: IResetNavigator) {
        addNavigator = navigator
    }

    fun cancelJob(){
        masterDataRepository.cancelJob()
        repository.cancelJob()
    }
}
