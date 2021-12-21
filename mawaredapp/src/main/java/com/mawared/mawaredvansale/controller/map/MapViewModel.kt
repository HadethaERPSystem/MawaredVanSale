package com.mawared.mawaredvansale.controller.map

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Call_Cycle
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.md.SalesmanSummary
import com.mawared.mawaredvansale.data.db.entities.reports.customer.CustomerStatus
import com.mawared.mawaredvansale.interfaces.IDatePicker
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository

class MapViewModel(private val masterDataRepository: IMDataRepository) : BaseViewModel() {

    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    var navigator: IMainNavigator<Call_Cycle>? = null
    val dtSelected : MutableLiveData<String> = MutableLiveData()
    var dateNavigator: IDatePicker? = null
    var totCust: MutableLiveData<Int?> = MutableLiveData()
    var totVisCust: MutableLiveData<Int?> = MutableLiveData()
    var totUVisCust: MutableLiveData<Int?> = MutableLiveData()
    var cuName: MutableLiveData<String?> = MutableLiveData()
    var cuPhone: MutableLiveData<String?> = MutableLiveData()
    var cuVisit: MutableLiveData<String?> = MutableLiveData()
    var cuVisitDate: MutableLiveData<String?> = MutableLiveData()
    var cuBalance: MutableLiveData<Double?> = MutableLiveData()

    var cu_Id: Int? = null
    var cu_dayname: String? = null
    var cu_isVisited: MutableLiveData<String?> = MutableLiveData("N")

    var places: List<Customer>? = null


    val customerList :LiveData<List<Customer>> = Transformations.switchMap(dtSelected) {
        masterDataRepository.customers_getPlaces(_sm_id, returnUKDateString(it))
    }

    var cu_ref_Id: MutableLiveData<Int> = MutableLiveData()
    val selectedCustomer : LiveData<CustomerStatus> = Transformations.switchMap(cu_ref_Id){
        masterDataRepository.getCustomerStatus(it)
    }
    val salesmanSum: LiveData<SalesmanSummary> = Transformations.switchMap(dtSelected){
        masterDataRepository.salesman_getSummary(_sm_id, returnUKDateString(it))
    }

    fun onSelectDate(v: View) {
        dateNavigator?.ShowDatePicker(v)
    }

    fun onVisit(){
        val baseEo = Call_Cycle(null, null, cu_Id, _sm_id, null, null, cu_dayname,
            null, null, null, null, null, null, null, null)
        navigator?.onItemEditClick(baseEo)
    }

    fun isVisibile(isShow: String?): Int{
        if(isShow == "Y")
            return View.VISIBLE
        else
            return View.GONE
    }

    fun cancelJob(){
        masterDataRepository.cancelJob()
    }
}