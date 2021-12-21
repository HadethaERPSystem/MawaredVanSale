package com.mawared.mawaredvansale.controller.reports.kpi

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.controller.reports.fms.SearchFilter
import com.mawared.mawaredvansale.data.db.entities.md.Lookups
import com.mawared.mawaredvansale.data.db.entities.md.SalesPlan
import com.mawared.mawaredvansale.data.db.entities.reports.dashboard.sm_dash1
import com.mawared.mawaredvansale.data.db.entities.reports.dashboard.sm_dash2
import com.mawared.mawaredvansale.interfaces.IDateRangePicker
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.reports.dashboard.IDashboardRepository

class KpiViewModel(private val repository: IDashboardRepository, private val masterDataRepository: IMDataRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    val dtFrom : MutableLiveData<String> = MutableLiveData()
    val dtTo : MutableLiveData<String> = MutableLiveData()
    var SearchFilter: MutableLiveData<SearchFilter> = MutableLiveData()
    var dateNavigator: IDateRangePicker? = null
    var errorMessage: MutableLiveData<String> = MutableLiveData()


    val planList: LiveData<List<Lookups>> by lazy{
        masterDataRepository.getSalesPaln()
    }
    val kpi_sm: LiveData<sm_dash1> = Transformations.switchMap(SearchFilter){
        repository.getDashboard_TotalCustomers(_sm_id, it.dtFrom!!, it.dtTo!!)
    }
    var planId : MutableLiveData<Int> = MutableLiveData(0)

    val kpi_cus: LiveData<sm_dash2> = Transformations.switchMap(planId){
        repository.getDashboard_SalesPlanning(_sm_id, it!!)
    }

    fun onFromDate(v: View) {
        dateNavigator?.fromDatePicker(v)
    }

    fun onToDate(v: View) {
        dateNavigator?.toDatePicker(v)
    }

    fun doApplyDateFilter(){
        SearchFilter.value = SearchFilter(0, dtFrom.value, dtTo.value)
    }

    fun doApplyPlanFilter(){

    }
}