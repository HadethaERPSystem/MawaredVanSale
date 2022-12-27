package com.mawared.mawaredvansale.controller.marketplace

import androidx.lifecycle.MutableLiveData
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.controller.helpers.extension.toFormatNumber
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.sales.OrderItems
import com.mawared.mawaredvansale.services.repositories.OrderRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import java.lang.Exception

class MarketPlaceViewModel(private val orderRepository: OrderRepository) : BaseViewModel() {

    var saleQty = MutableLiveData<String>()
    var giftQty = MutableLiveData<String>()
    var saleAmount = MutableLiveData<String>()

    var customer : Customer? = null
    var vocode : String? = null
    var onlyBrowsing : Boolean = false

    var orders: List<OrderItems> = arrayListOf()
    private fun loadOrders(Success:(() -> Unit) = {}) {
        try {
            Coroutines.io {
                orders = orderRepository.getOrderItems()
                Success()
            }

        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    fun refresh(){
        loadOrders(){
            val qty : Double = orders.sumByDouble {  if(it.od_unit_qty == null) 0.0 else it.od_unit_qty!! }
            val gqty : Double = orders.sumByDouble { it.od_gift_qty!! }
            val amount : Double = orders.sumByDouble { it.od_net_total!! }

            saleQty.postValue(qty.toFormatNumber())
            giftQty.postValue(gqty.toFormatNumber())
            saleAmount.postValue( "${amount.toFormatNumber()} ${App.prefs.saveUser!!.sl_cr_code!!}")
        }

    }
}