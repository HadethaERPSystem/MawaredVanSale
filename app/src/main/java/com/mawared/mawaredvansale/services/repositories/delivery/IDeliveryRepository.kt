package com.mawared.mawaredvansale.services.repositories.delivery

import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery_Items

interface IDeliveryRepository {
    fun update(baseEo: Delivery) : LiveData<Delivery>
    fun getSalesmanId(sm_Id: Int, cu_Id: Int?) : LiveData<List<Delivery>>
    fun getById(dl_Id: Int): LiveData<Delivery>
    fun getItemByMasterId(dl_Id: Int): LiveData<List<Delivery_Items>>

    fun cancelJob()
}