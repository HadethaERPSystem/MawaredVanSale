package com.mawared.mawaredvansale.controller.sales.order.orderslist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.order.IOrderRepository

class OrdersViewModel(private val orderRepository: IOrderRepository) : BaseViewModel() {

    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    var navigator: IMainNavigator<Sale_Order>? = null
    var errorMessage: MutableLiveData<String> = MutableLiveData()

    private val cu_id: MutableLiveData<Int?> = MutableLiveData()

    val orders: LiveData<PagedList<Sale_Order>> = Transformations.switchMap(cu_id) {
        orderRepository.fetchLiveOrdersPagedList(_sm_id, it, "SaleOrder")
    }

    val networkStateRV: LiveData<NetworkState> by lazy {
        orderRepository.getOrderNetworkState()
    }

    fun listIsEmpty():Boolean{
        return orders.value?.isEmpty() ?: true
    }
    private val _so_Id_for_delete: MutableLiveData<Int> = MutableLiveData()
    val deleteRecord: LiveData<String> = Transformations
        .switchMap(_so_Id_for_delete){
            orderRepository.delete(it)
        }

    // using to refresh recycler view
    fun setCustomer(cm_Id: Int?){
        if(cu_id.value == cm_Id && cu_id.value != null){
            return
        }
        cu_id.value = cm_Id
    }

    fun refresh(){
        setCustomer(cu_id.value)
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

    fun checkStatus(baseEo: Sale_Order): Boolean{
        val value = (baseEo.so_status_code != "Invoice")
        return  value
    }

    fun cancelJob(){
        orderRepository.cancelJob()
    }

    override fun onCleared() {
        super.onCleared()
        orderRepository.cancelJob()
    }
}
