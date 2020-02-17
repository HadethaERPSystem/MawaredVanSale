package com.mawared.mawaredvansale.controller.surveyentry

import android.content.res.Resources
import android.location.Location
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.md.Voucher
import com.mawared.mawaredvansale.data.db.entities.srv.Question
import com.mawared.mawaredvansale.data.db.entities.srv.Survey_Detail
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.srv.SurveyRepositoryImp
import com.mawared.mawaredvansale.utilities.lazyDeferred

class SurveyEntryViewModel(private val repositoryImp: SurveyRepositoryImp, private val masterDataRepository: IMDataRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_id != null)  App.prefs.savedSalesman!!.sm_id else 0
    var mode: String = "Add"
    var msgListener: IMessageListener? = null

    var addNavigator: IAddNavigator<Survey_Detail>? = null
    var resources: Resources? = null

    // google map location GPS
    var location: Location? = null

    var userName: String? = App.prefs.saveUser?.user_name
    var clientName: String? = App.prefs.saveUser?.client_name ?: "AL-NADER Co."
    val questions by lazyDeferred {
        repositoryImp.getSurvey()
    }

    var qnEoList: List<Question> = arrayListOf()

    var mSrv_name: MutableLiveData<String> = MutableLiveData()
    var mSrv_vst_no: MutableLiveData<String> = MutableLiveData()
    var mSrv_vst_date: MutableLiveData<String> = MutableLiveData()
    var mSrv_next_vst_date: MutableLiveData<String> = MutableLiveData()

    var mQustion_Name: MutableLiveData<String> = MutableLiveData()
    var mAnswer_text: MutableLiveData<String> = MutableLiveData()
    var mAnswer_area: MutableLiveData<String> = MutableLiveData()
    var mRadio_checked: MutableLiveData<Int> = MutableLiveData()

    var selectedCustomer: Customer? = null

    var term: MutableLiveData<String> = MutableLiveData()
    val customerList: LiveData<List<Customer>> = Transformations.switchMap(term) {
        masterDataRepository.getCustomers(_sm_id, it)
    }

    var voucher: Voucher? = null
    private val _vo_code: MutableLiveData<String> = MutableLiveData()
    val mVoucher: LiveData<Voucher> = Transformations
        .switchMap(_vo_code){
            masterDataRepository.getVoucherByCode(it)
        }

    // Set functions
    fun setVoucherCode(vo_code: String){
        if(_vo_code.value == vo_code){
            return
        }
        _vo_code.value = vo_code
    }

    fun onDatePicker(v: View) {
        addNavigator?.onShowDatePicker(v)
    }

    fun clear(code: String) {
        when(code) {
            "cu"-> {
                selectedCustomer = null
            }
            "prod"-> {

            }
        }
        addNavigator?.clear(code)
    }

    fun cancelJob(){
        masterDataRepository.cancelJob()
        //saleRepository.cancelJob()
    }
}
