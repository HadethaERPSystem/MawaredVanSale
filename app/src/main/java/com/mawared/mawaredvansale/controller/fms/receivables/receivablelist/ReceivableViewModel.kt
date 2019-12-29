package com.mawared.mawaredvansale.controller.fms.receivables.receivablelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.fms.IReceivableRepository

class ReceivableViewModel(private val repository: IReceivableRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0

    var navigator: IMainNavigator<Receivable>? = null
    var msgListener: IMessageListener? = null

    private val _cu_id: MutableLiveData<Int> = MutableLiveData()

    val baseEoList: LiveData<List<Receivable>> = Transformations
        .switchMap(_cu_id){
            repository.getReceivable(_sm_id, it)
        }

    private val _rcv_Id_for_delete: MutableLiveData<Int> = MutableLiveData()
    val deleteRecord: LiveData<String> = Transformations
        .switchMap(_rcv_Id_for_delete){
            repository.delete(it)
        }
    // set functions to refresh data
    fun setCustomer(cm_Id: Int?){
        if(_cu_id.value == cm_Id && cm_Id != null){
            return
        }
        _cu_id.value = cm_Id
    }

    // confirm delete
    fun confirmDelete(baseEo: Receivable){
        _rcv_Id_for_delete.value = baseEo.rcv_Id
    }

    fun onItemDelete(baseEo: Receivable)
    {
        navigator?.onItemDeleteClick(baseEo)
    }

    fun onItemEdit(baseEo: Receivable)
    {
        navigator?.onItemEditClick(baseEo)
    }

    fun onItemView(baseEo: Receivable)
    {
        navigator?.onItemViewClick(baseEo)
    }

    fun cancelJob(){
        repository.cancelJob()
    }
}
