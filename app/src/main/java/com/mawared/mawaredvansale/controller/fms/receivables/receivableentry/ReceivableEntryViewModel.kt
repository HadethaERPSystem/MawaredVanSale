package com.mawared.mawaredvansale.controller.fms.receivables.receivableentry

import android.content.res.Resources
import android.location.Location
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.data.db.entities.md.Currency
import com.mawared.mawaredvansale.data.db.entities.md.Currency_Rate
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.md.Voucher
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.fms.IReceivableRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.utilities.lazyDeferred
import org.threeten.bp.LocalDate

class ReceivableEntryViewModel(private val repository: IReceivableRepository,
                               private val masterDataRepository: IMDataRepository) : BaseViewModel() {

    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_id != null)  App.prefs.savedSalesman!!.sm_id else 0
    var mode: String = "Add"
    var resources: Resources? = null

    var msgListener: IMessageListener? = null
    var addNavigator: IAddNavigator<Receivable>? = null

    // google map location GPS
    var location: Location? = null
    var doc_no: MutableLiveData<String> = MutableLiveData()
    var doc_date: MutableLiveData<String> = MutableLiveData()
    var bc_amount: MutableLiveData<String> = MutableLiveData()
    var bc_change: MutableLiveData<String> = MutableLiveData()
    var lc_amount: MutableLiveData<String> = MutableLiveData()
    var lc_change: MutableLiveData<String> = MutableLiveData()
    var comment: MutableLiveData<String> = MutableLiveData()

    private val _baseEo: MutableLiveData<Receivable> = MutableLiveData()
    val savedEntity: LiveData<Receivable> = Transformations
        .switchMap(_baseEo){
            repository.insert(it)
        }

    // for load order for edit or view
    var _entityEo: Receivable? = null
    private val rcv_Id : MutableLiveData<Int> = MutableLiveData()
    val entityEo: LiveData<Receivable> = Transformations
        .switchMap(rcv_Id){
            repository.getById(it)
        }

    var selectedCustomer: Customer? = null
    val customerList by lazyDeferred {
        masterDataRepository.getCustomers(_sm_id)
    }

    var rate : Double = 0.00
    private val _cr_Id: MutableLiveData<Int> = MutableLiveData()
    val currencyRate: LiveData<Currency_Rate> = Transformations
        .switchMap(_cr_Id) {
            masterDataRepository.getRate(it)
        }

    var voucher: Voucher? = null
    private val _vo_code: MutableLiveData<String> = MutableLiveData()
    val mVoucher: LiveData<Voucher> = Transformations
        .switchMap(_vo_code){
            masterDataRepository.getVoucherByCode(it)
        }

    var bcCurrency: Currency? = null
    private val _sale_cr_symbole: MutableLiveData<String> = MutableLiveData()
    val saleCurrency: LiveData<Currency> = Transformations
        .switchMap(_sale_cr_symbole){
            masterDataRepository.getCurrencyByCode(it)
        }

    var lcCurrency: Currency? = null
    private val _nd_cr_symbol: MutableLiveData<String> = MutableLiveData()
    val ndCurrency: LiveData<Currency> = Transformations
        .switchMap(_nd_cr_symbol){
            masterDataRepository.getCurrencyByCode(it)
        }

    // set function
    fun setReceivableId(id: Int){
        if(rcv_Id.value == id){
            return
        }
        rcv_Id.value = id
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

    fun setSaleCurrency(cr_code: String){
        if(_sale_cr_symbole.value == cr_code){
            return
        }
        _sale_cr_symbole.value = cr_code
    }

    fun setSecondCurrency(cr_code: String){
        if(_nd_cr_symbol.value == cr_code){
            return
        }
        _nd_cr_symbol.value = cr_code
    }

    // operation method
    fun onSave(){
        if(isValid()){
            try {
                val user = App.prefs.saveUser
                val strDate: LocalDate = LocalDate.now()
                val amount_usd: Double = if(bc_amount.value != null)  bc_amount.value!!.toDouble() else 0.00
                val amount_iqd: Double = if(lc_amount.value != null)  lc_amount.value!!.toDouble() else 0.00
                val change_usd: Double = if(bc_change.value != null)  bc_change.value!!.toDouble() else 0.00
                val change_iqd: Double = if(lc_change.value != null)  lc_change.value!!.toDouble() else 0.00
                val baseEo = Receivable(
                    user?.cl_Id, user?.org_Id, 0, doc_date.value, mVoucher.value!!.vo_Id, "${mVoucher.value!!.vo_prefix}",
                    null, _sm_id, bcCurrency?.cr_id, 0.00,amount_usd, amount_iqd,
                    change_usd, change_iqd, bcCurrency?.cr_id, lcCurrency?.cr_id, rate, comment.value, "N",
                    location?.latitude, location?.longitude,"$strDate", "${user?.id}","$strDate", "${user?.id}"
                )

                _baseEo.value = baseEo
            }
            catch (e: Exception){
                msgListener?.onFailure("${resources!!.getString(R.string.msg_exception)} Exception is ${e.message}")
            }
        }
    }


    private fun isValid(): Boolean{
        var isSuccessful = true
        var msg: String? = null

        if(doc_date.value.isNullOrEmpty()){
            msg = resources!!.getString(R.string.msg_error_invalid_date)
        }
        if(bc_amount.value.isNullOrEmpty() && lc_amount.value.isNullOrEmpty()){
            msg += "\n\r" + resources!!.getString(R.string.msg_error_invalid_received_Amount)
        }
        if(selectedCustomer == null){
            msg += "\n\r" + resources!!.getString(R.string.msg_error_no_customer)
        }

        if(!msg.isNullOrEmpty()){
            isSuccessful = false
            msgListener?.onFailure(msg)
        }
        return isSuccessful
    }

    fun onNew(){
        doc_no.value = ""
        doc_date.value = "${LocalDate.now()}"
        clear("cu")
        bc_amount.value = "0"
        lc_amount.value = "0"
        bc_change.value = "0"
        lc_change.value = "0"
        comment.value = ""
    }

    /////////////////////////////
    fun clear(code: String) {
        selectedCustomer = null
        addNavigator?.clear(code)
    }

    fun onDatePicker(v: View) {
        addNavigator?.onShowDatePicker(v)
    }

    fun cancelJob(){
        masterDataRepository.cancelJob()
        repository.cancelJob()
    }
}
