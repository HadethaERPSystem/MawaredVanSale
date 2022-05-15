package com.mawared.mawaredvansale.controller.marketplace.schedulecustomer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.sales.Order
import com.mawared.mawaredvansale.services.repositories.OrderRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.utilities.Coroutines

class SelectCustomerViewModel (private val repository: IMDataRepository, private val orderRepository: OrderRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    var term: String? = ""

    fun loadData(list: MutableList<Customer>, term: String,pageCount: Int, loadMore: (List<Customer>?, Int)->Unit){
        try {
            Coroutines.ioThenMain({
                val cu = repository.getCustomersOnPages(_sm_id, null, term, pageCount)
                if(cu != null){
                    list.addAll(cu)

                }
            },
            {loadMore(list, pageCount)})
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun deleteAll(Success:() -> Unit = {}, Fail:() -> Unit = {}){
        try {
            Coroutines.ioThenMain({
                orderRepository.deleteAllItems()
            },
                {
                    Success()
                })
        }catch (e: java.lang.Exception){
            e.printStackTrace()
            Fail()
        }
    }
}