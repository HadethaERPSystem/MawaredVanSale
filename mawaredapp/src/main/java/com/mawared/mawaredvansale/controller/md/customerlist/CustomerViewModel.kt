package com.mawared.mawaredvansale.controller.md.customerlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository

class CustomerViewModel(private val repository:IMDataRepository) : BaseViewModel() {

    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0

    var navigator: IMainNavigator<Customer>? = null
    var msgListener: IMessageListener? = null
    var errorMessage: MutableLiveData<String> = MutableLiveData()

    //val _cu_Id: MutableLiveData<Int> = MutableLiveData()
    var searchFilter: MutableLiveData<CustomerFilter> = MutableLiveData()
    val baseEoList: LiveData<PagedList<Customer>> = Transformations
        .switchMap(searchFilter){
            repository.fetchCustomerOnPages(_sm_id, it.org_Id, it.term)
        }

    val networkStateRV: LiveData<NetworkState> by lazy {
        repository.getCustomerNetworkState()
    }

    val networkState: LiveData<NetworkState> by lazy {
        repository.networkState
    }

    fun listIsEmpty():Boolean{
        return baseEoList.value?.isEmpty() ?: true
    }

    fun onItemEdit(baseEo: Customer)
    {
        navigator?.onItemEditClick(baseEo)
    }

    fun onItemView(baseEo: Customer)
    {
        navigator?.onItemViewClick(baseEo)
    }

    fun cancelJob(){
        repository.cancelJob()
    }

    fun doSearch(org_Id: Int?, term: String){
        searchFilter.value = CustomerFilter(org_Id, term)
    }
}
