package com.mawared.mawaredvansale.controller.reports.customer

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.controller.reports.fms.SearchFilter
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.reports.customer.CustomerStatement
import com.mawared.mawaredvansale.interfaces.IDateRangePicker
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.reports.customer.ICuStatementRepository

class CustomerStatementViewModel(private val repository: ICuStatementRepository, private val masterDataRepository:IMDataRepository) : BaseViewModel(){
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    val dtFrom : MutableLiveData<String> = MutableLiveData()
    val dtTo : MutableLiveData<String> = MutableLiveData()
    var SearchFilter: MutableLiveData<SearchFilter> = MutableLiveData()
    var dateNavigator: IDateRangePicker? = null
    var errorMessage: MutableLiveData<String> = MutableLiveData()

    var selectedCustomer: Customer? = null
    val term : MutableLiveData<String> = MutableLiveData()
    val customerList: LiveData<List<Customer>> = Transformations.switchMap(term){
        masterDataRepository.getCustomersByOrg(App.prefs.saveUser!!.org_Id, it)
    }

    val csItems: LiveData<PagedList<CustomerStatement>> = Transformations
        .switchMap(SearchFilter){
            repository.fetchLivePagedList(_sm_id, it.cu_Id, it.dtFrom, it.dtTo)
        }

    val networkState by lazy {
        repository.getRecNetworkState()
    }

    fun listIsEmpty():Boolean{
        return csItems.value?.isEmpty() ?: true
    }

    fun onFromDate(v: View) {
        dateNavigator?.fromDatePicker(v)
    }

    fun onToDate(v: View) {
        dateNavigator?.toDatePicker(v)
    }

    fun doSearch(){
        val cu_Id = selectedCustomer?.cu_ref_Id ?: 0
        SearchFilter.value = SearchFilter(cu_Id, dtFrom.value, dtTo.value)
    }
}
