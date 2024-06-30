package com.mawared.mawaredvansale.controller.md.customerentry

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
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.lazyDeferred
import org.threeten.bp.LocalDateTime

class CustomerEntryViewModel(private val repository: IMDataRepository) : BaseViewModel() {
    private val smId: Int = if(App.prefs.savedSalesman?.sm_id != null)  App.prefs.savedSalesman!!.sm_id else 0
    var mode: String = "Add"
    var resources: Resources? = null
    var isRunning: Boolean = false
    var msgListener: IMessageListener? = null
    var addNavigator: IAddNavigator<Customer>? = null

    var location: Location? = null
    // view model fields for entry layout
    var mcu_code: MutableLiveData<String> = MutableLiveData()
    var mcu_barcode: MutableLiveData<String> = MutableLiveData()
    var mcu_name_ar: MutableLiveData<String> = MutableLiveData()
    var mcu_name: MutableLiveData<String> = MutableLiveData()
    var mcu_trade_name: MutableLiveData<String> = MutableLiveData()
    var mcu_address_ar: MutableLiveData<String> = MutableLiveData()
    var mcu_address: MutableLiveData<String> = MutableLiveData()
    var mcu_phone: MutableLiveData<String> = MutableLiveData()
    var mcu_mobile: MutableLiveData<String> = MutableLiveData()
    var mcu_contact_name: MutableLiveData<String> = MutableLiveData()
    var mcu_notes: MutableLiveData<String> = MutableLiveData()
    var mcu_balance: MutableLiveData<String> = MutableLiveData()
    var mcu_credit_limit: MutableLiveData<String> = MutableLiveData()
    var mcu_credit_limit_days: MutableLiveData<String> = MutableLiveData()
    var mcu_payment_terms: MutableLiveData<String> = MutableLiveData()
    var mcu_longitude: MutableLiveData<String> = MutableLiveData()
    var mcu_latitude:MutableLiveData<String> = MutableLiveData()

    ///////////////////////////////////////
    /// Autocomplete object for Customer Payment Type
    var selectedCPT: Customer_Payment_Type? = null
    val cpt_List by lazyDeferred {
        repository.getCptAll("")
    }

    var selectedCustomerCat: Customer_Category? = null
    val Category_List by lazyDeferred {
        repository.customersCategory_GetByTerm("")
    }

    var selectedPriceCategory: PriceCategory? = null
    val priceCatList by lazyDeferred {
        repository.priceCat_GetBySalesman(smId)
    }
    var selectedRegion: Region? = null
    val reginList by lazyDeferred {
        repository.getRegions()
    }
    ///////////////////////////////////////
    /// For insert or update current customer
    var _baseEo: MutableLiveData<Customer> = MutableLiveData()


    // for load order for edit or view
    var _entityEo: Customer? = null
    private val cu_Id : MutableLiveData<Int> = MutableLiveData()
    val entityEo: LiveData<Customer> = Transformations
        .switchMap(cu_Id){
            repository.getCustomerById(it)
        }

    ///////////////////////////////////////////////////////////
    /// set function for load customer
    fun setCustomerId(id: Int){
        if(id == cu_Id.value){
            return
        }
        cu_Id.value = id
    }

    // operation method
    // Function Name: Save customer
    fun onSave(){
        if(isValid()){
            try {
                val user = App.prefs.saveUser
                val strDate = LocalDateTime.now()
                val balance: Double? = mcu_balance.value?.toDouble()
                val limit: Double? = mcu_credit_limit.value?.toDouble()
                val limit_days: Int? = mcu_credit_limit_days.value?.toInt()
                val cpt_Id = selectedCPT?.cpt_Id ?: _entityEo?.cu_payment_Id
                val cat_Id = selectedCustomerCat?.cat_Id ?: _entityEo?.cu_cat_Id
                val cu_rg_Id = selectedRegion?.rg_id ?: _entityEo?.cu_rg_Id
                val cu_price_cat_Id = selectedPriceCategory?.prc_Id ?: _entityEo?.cu_price_cat_Id
                val baseEo = Customer(mcu_code.value, null, cpt_Id, user?.cl_Id, user?.org_Id,
                    mcu_barcode.value, mcu_name_ar.value, mcu_name.value, mcu_trade_name.value, mcu_address_ar.value, mcu_address.value,
                    mcu_phone.value, mcu_mobile.value, mcu_contact_name.value,  cat_Id, cu_rg_Id, null,mcu_notes.value, null,
                    balance, limit, limit_days, mcu_payment_terms.value, mcu_latitude.value?.toDouble(), mcu_longitude.value?.toDouble(), cu_price_cat_Id,
                    "$strDate", "${user?.id}", "$strDate","${user?.id}"
                )
                if(_entityEo != null){
                    baseEo.cu_Id = _entityEo!!.cu_Id
                    baseEo.cu_ref_Id = _entityEo!!.cu_ref_Id
                }

                Coroutines.main {
                    try {
                        val response = repository.customerSaveOrUpdate(baseEo)
                        if(response.isSuccessful){
                            _baseEo.value = response.data
                            isRunning = false
                        }
                        else{
                            isRunning = false
                            msgListener?.onFailure("Error message when try to save customer. Error is ${response.message}")
                        }
                    }catch (e: Exception){
                        isRunning = false
                        msgListener?.onFailure("Error message when try to save customer. Error is ${e.message}")
                    }
                }
            }
            catch (e: Exception){
                msgListener?.onFailure("${resources!!.getString(R.string.msg_exception)} Exception is ${e.message}")
            }
        }else{
            isRunning = false
        }
    }


    private fun isValid(): Boolean{
        var isSuccessful = true
        var msg: String? = ""

        if(mcu_name_ar.value.isNullOrEmpty()){
            msg = resources!!.getString(R.string.msg_error_cu_name)
        }

//        if(mcu_trade_name.value.isNullOrEmpty()){
//            msg = (if(msg!!.length > 0) "\n\r" else "")  +  resources!!.getString(R.string.msg_error_cu_trade_name)
//        }

        if(selectedCPT == null && _entityEo == null){
            msg += (if(msg!!.length > 0) "\n\r" else "")  +  resources!!.getString(R.string.msg_error_cu_payment_type)
        }
        if(selectedCustomerCat == null && _entityEo == null){
            msg += (if(msg!!.length > 0) "\n\r" else "")  +  resources!!.getString(R.string.msg_error_cu_group)
        }

        if(selectedPriceCategory == null && _entityEo == null){
            msg += (if(msg!!.length > 0) "\n\r" else "")  +  resources!!.getString(R.string.msg_error_price_category)
        }

        if(selectedRegion == null && _entityEo == null){
            msg += (if(msg!!.length > 0) "\n\r" else "")  +  resources!!.getString(R.string.msg_error_region)
        }

        if(mcu_longitude.value.isNullOrEmpty() && mcu_latitude.value.isNullOrEmpty()) {
            msg += (if(msg!!.length > 0) "\n\r" else "")  +  resources!!.getString(R.string.msg_error_logtude_latitude)
        }

        if(mcu_address_ar.value.isNullOrEmpty()){
            msg += (if(msg!!.length > 0) "\n\r" else "")  +  resources!!.getString(R.string.msg_error_address)
        }

        if(mcu_mobile.value.isNullOrEmpty() && mcu_phone.value.isNullOrEmpty()){
            msg += (if(msg!!.length > 0) "\n\r" else "")  +  resources!!.getString(R.string.msg_error_mobile)
        }

        if(!msg.isNullOrEmpty()){
            isSuccessful = false
            msgListener?.onFailure(msg)
        }
        return isSuccessful
    }

    fun onNew(){
        mcu_code.value = ""
        mcu_barcode.value = ""
        mcu_name_ar.value = ""
        mcu_name.value = ""
        mcu_trade_name.value = ""
        mcu_address_ar.value = ""
        mcu_address.value = ""
        mcu_phone.value = ""
        mcu_mobile.value = ""
        mcu_contact_name.value = ""
        mcu_notes.value = ""
        mcu_balance.value = ""
        mcu_credit_limit.value = ""
        mcu_payment_terms.value = ""
        selectedPriceCategory = null
        selectedCPT = null
        //selectedCustomerGroup = null
        mcu_longitude.value = "${location?.latitude}"
        mcu_latitude.value = "${location?.longitude}"

        clear("cpt")
        clear("cg")
    }

    /////////////////////////////
    fun clear(code: String) {
        when(code) {
            "cpt"-> {
                selectedCPT = null
            }
            "cat" -> selectedCustomerCat = null
            "cg"-> {
               // selectedCustomerGroup = null
            }
            "prcode" -> {
                selectedPriceCategory = null
            }
            "rg" ->{
                selectedRegion = null
            }
        }
        addNavigator?.clear(code)
    }

    fun displayLocation(){
        mcu_latitude.value = "${location?.latitude}"
        mcu_longitude.value = "${location?.longitude}"
    }

    fun onDatePicker(v: View) {
        addNavigator?.onShowDatePicker(v)
    }

    fun cancelJob(){
        repository.cancelJob()
    }
}

