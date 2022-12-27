package com.mawared.mawaredvansale.controller.sales.order.orderslist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.order.IOrderRepository
import com.mawared.mawaredvansale.utilities.Coroutines

class OrdersViewModel(private val orderRepository: IOrderRepository) : BaseViewModel() {

    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0

    var errorMessage: MutableLiveData<String> = MutableLiveData()

    var cu_id: Int? = null
    var term: String? = ""


    fun loadData(list: MutableList<Sale_Order>, term: String, cu_id: Int?, pageCount: Int, loadMore: (List<Sale_Order>?, Int) -> Unit){
        try {
            Coroutines.ioThenMain({
                val tmp = orderRepository.getOrderOnPages(_sm_id, cu_id, "SaleOrder", term, pageCount)
                if(tmp != null){
                    list.addAll(tmp)
                }
            }, {loadMore(list, pageCount)})
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    private val _so_Id_for_delete: MutableLiveData<Int> = MutableLiveData()
    val deleteRecord: LiveData<String> = Transformations
        .switchMap(_so_Id_for_delete){
            orderRepository.delete(it)
        }

    // confirm delete
    fun confirmDelete(baseEo: Sale_Order){
        _so_Id_for_delete.value = baseEo.so_id
    }

    fun cancelJob(){
        orderRepository.cancelJob()
    }

    override fun onCleared() {
        super.onCleared()
        orderRepository.cancelJob()
    }
}
