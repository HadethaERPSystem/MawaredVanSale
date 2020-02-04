package com.mawared.mawaredvansale.controller.sales.salereturn.salereturnlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.services.repositories.salereturn.ISaleReturnRepository

class SaleReturnViewModel(private val repository: ISaleReturnRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    var navigator: IMainNavigator<Sale_Return>? = null

    private val _cu_id: MutableLiveData<Int> = MutableLiveData()

    val saleReturns: LiveData<List<Sale_Return>> = Transformations
        .switchMap(_cu_id){
            repository.getSaleReturn(_sm_id, it)
        }

    private val _sr_Id_for_delete: MutableLiveData<Int> = MutableLiveData()
    val deleteRecord: LiveData<String> = Transformations
        .switchMap(_sr_Id_for_delete){
            repository.delete(it)
        }

    fun setCustomer(cm_Id: Int?){
        if(_cu_id.value == cm_Id && cm_Id != null){
            return
        }
        _cu_id.value = cm_Id
    }

    // on press delete button in recycler view
    fun onItemDelete(baseEo: Sale_Return)
    {
        navigator?.onItemDeleteClick(baseEo)
    }

    // confirm delete sale return record
    fun confirmDelete(baseEo: Sale_Return){
        _sr_Id_for_delete.value = baseEo.sr_Id
    }

    // on press edit button in recycler view
    fun onItemEdit(baseEo: Sale_Return)
    {
        navigator?.onItemEditClick(baseEo)
    }

    // on press view button in recycler view
    fun onItemView(baseEo: Sale_Return)
    {
        navigator?.onItemViewClick(baseEo)
    }

    fun cancelJob(){
        repository.cancelJob()
    }
}
