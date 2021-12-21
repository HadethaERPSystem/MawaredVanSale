package com.mawared.mawaredvansale.controller.reports.fms

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.reports.fms.CashbookStatement
import com.mawared.mawaredvansale.interfaces.IDateRangePicker
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.reports.fms.ICashbookRepository

class CashbookStatementViewModel(private val repository: ICashbookRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    val dtFrom : MutableLiveData<String> = MutableLiveData()
    val dtTo : MutableLiveData<String> = MutableLiveData()
    var dateFilter: MutableLiveData<DateFilter> = MutableLiveData()
    var errorMessage: MutableLiveData<String> = MutableLiveData()
    var dateNavigator: IDateRangePicker? = null

    val cbsItems: LiveData<PagedList<CashbookStatement>> = Transformations
        .switchMap(dateFilter) {
        repository.fetchData(_sm_id, it.dtFrom , it.dtTo)
    }

    val networkState: LiveData<NetworkState> by lazy {
        repository.getCashNetworkState()
    }

    fun listIsEmpty():Boolean{
        return cbsItems.value?.isEmpty() ?: true
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
