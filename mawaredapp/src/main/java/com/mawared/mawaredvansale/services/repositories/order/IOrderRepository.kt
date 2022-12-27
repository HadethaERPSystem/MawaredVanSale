package com.mawared.mawaredvansale.services.repositories.order

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order_Items
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle
import com.mawared.mawaredvansale.services.repositories.NetworkState

interface IOrderRepository {

    val networkState: LiveData<NetworkState>

    fun insert(baseEo: Sale_Order) : LiveData<Sale_Order>
    suspend fun SaveOrUpdate(baseEo: Sale_Order) : ResponseSingle<Sale_Order>
    // get all order for specific salesmand and customer
    fun getOrder(sm_Id: Int, cu_Id: Int?, vo_code: String) : LiveData<List<Sale_Order>>
    suspend fun getOrderOnPages(sm_Id: Int, cu_Id: Int?, vo_code: String, term: String, page: Int) : List<Sale_Order>?

    // get by id
    fun getOrderById(so_Id: Int): LiveData<Sale_Order>
    // delete order
    fun delete(so_Id: Int) : LiveData<String>
    // get all order items for specific order
    fun getItemByOrderId(so_Id: Int): LiveData<List<Sale_Order_Items>>
    // cancel current request
    fun cancelJob()
}