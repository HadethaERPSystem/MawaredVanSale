package com.mawared.mawaredvansale.controller.reports.sales

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.controller.reports.fms.DateFilter
import com.mawared.mawaredvansale.data.db.entities.reports.sales.SalesStatement
import com.mawared.mawaredvansale.interfaces.IDateRangePicker
import com.mawared.mawaredvansale.services.repositories.reports.sales.ISalesRepository

class SalesStatementViewModel(private val repository: ISalesRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    val dtFrom : MutableLiveData<String> = MutableLiveData()
    val dtTo : MutableLiveData<String> = MutableLiveData()
    var dateFilter: MutableLiveData<DateFilter> = MutableLiveData()
    var dateNavigator: IDateRangePicker? = null
    var errorMessage: MutableLiveData<String> = MutableLiveData()
    val sales: LiveData<PagedList<SalesStatement>> = Transformations
        .switchMap(dateFilter){
            repository.fetchLivePagedList(_sm_id, it.dtFrom, it.dtTo)
        }

    val rcv_networkState by lazy {
        repository.getRecNetworkState()
    }

    fun listIsEmpty():Boolean{
        return sales.value?.isEmpty() ?: true
    }

    fun onFromDate(v: View) {
        dateNavigator?.fromDatePicker(v)
    }

    fun onToDate(v: View) {
        dateNavigator?.toDatePicker(v)
    }

    fun doSearch(){
        dateFilter.value = DateFilter(dtFrom.value, dtTo.value)
    }
}
