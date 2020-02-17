package com.mawared.mawaredvansale.controller.fms.payables.payablelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.fms.Payable
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.fms.IPayableRepository

class PayableViewModel(private val repository: IPayableRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0

    var navigator: IMainNavigator<Payable>? = null
    var msgListener: IMessageListener? = null

    private val _cu_id: MutableLiveData<Int> = MutableLiveData()

    val baseEoList: LiveData<PagedList<Payable>> =Transformations.switchMap(_cu_id) {
        repository.fetchLivePagedList(_sm_id, it)
    }

    val networkStateRV: LiveData<NetworkState> by lazy {
        repository.getPayableNetworkState()
    }

    fun listIsEmpty():Boolean{
        return baseEoList.value?.isEmpty() ?: true
    }

    val networkState by lazy {
        repository.networkState
    }

    private val _py_Id_for_delete: MutableLiveData<Int> = MutableLiveData()
    val deleteRecord: LiveData<String> = Transformations
        .switchMap(_py_Id_for_delete){
            repository.delete(it)
        }


    fun setCustomer(cm_Id: Int?){
        if(_cu_id.value == cm_Id && cm_Id != null){
            return
        }
        _cu_id.value = cm_Id
    }

    // confirm delete
    fun confirmDelete(baseEo: Payable){
        _py_Id_for_delete.value = baseEo.py_Id
    }

    fun onItemDelete(baseEo: Payable)
    {
        navigator?.onItemDeleteClick(baseEo)
    }

    fun onItemEdit(baseEo: Payable)
    {
        navigator?.onItemEditClick(baseEo)
    }

    fun onItemView(baseEo: Payable)
    {
        navigator?.onItemViewClick(baseEo)
    }

    fun cancelJob(){
        repository.cancelJob()
    }
}
