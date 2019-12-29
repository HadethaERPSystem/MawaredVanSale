package com.mawared.mawaredvansale.controller.sales.psorder.psorderlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.services.repositories.order.IOrderRepository

class PSOrdersViewModel(private val orderRepository: IOrderRepository) : BaseViewModel() {

    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    var navigator: IMainNavigator<Sale_Order>? = null


    private val _cu_id: MutableLiveData<Int> = MutableLiveData()

    val orders: LiveData<List<Sale_Order>> = Transformations
        .switchMap(_cu_id){
            orderRepository.getOrder(_sm_id, it, "PSOrder")
        }

    private val _so_Id_for_delete: MutableLiveData<Int> = MutableLiveData()
    val deleteRecord: LiveData<String> = Transformations
        .switchMap(_so_Id_for_delete){
            orderRepository.delete(it)
        }

    // using to refresh recycler view
    fun setCustomer(cm_Id: Int?){
        if(_cu_id.value == cm_Id && cm_Id != null){
            return
        }
        _cu_id.value = cm_Id
    }

    // confirm delete
    fun confirmDelete(baseEo: Sale_Order){
        _so_Id_for_delete.value = baseEo.so_id
    }

    // on press deleted button in recycler view
    fun onItemDelete(sale: Sale_Order)
    {
        navigator?.onItemDeleteClick(sale)
    }

    // on press edit button in recycler view
    fun onItemEdit(baseEo: Sale_Order)
    {
        navigator?.onItemEditClick(baseEo)
    }

    // on press view button in recycler view
    fun onItemView(baseEo: Sale_Order)
    {
        navigator?.onItemViewClick(baseEo)
    }

    fun cancelJob(){
        orderRepository.cancelJob()
    }
}
