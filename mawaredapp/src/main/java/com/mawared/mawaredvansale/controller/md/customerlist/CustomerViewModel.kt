package com.mawared.mawaredvansale.controller.md.customerlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository

class CustomerViewModel(private val repository:IMDataRepository) : BaseViewModel() {

    private val _sm_id: MutableLiveData<Int> = MutableLiveData()

    var navigator: IMainNavigator<Customer>? = null
    var msgListener: IMessageListener? = null

    val baseEoList: LiveData<List<Customer>> = Transformations
        .switchMap(_sm_id){
            repository.getCustomers(it)
        }

    fun setSalesmanId(sm_Id: Int){
        if(sm_Id == _sm_id.value){
            return
        }
        _sm_id.value = sm_Id
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
}
