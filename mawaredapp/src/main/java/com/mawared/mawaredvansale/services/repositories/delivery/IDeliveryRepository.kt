package com.mawared.mawaredvansale.services.repositories.delivery

import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery_Items
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle

interface IDeliveryRepository {
    fun update1(baseEo: Delivery) : LiveData<Delivery>
    suspend fun update(baseEo: Delivery): ResponseSingle<Delivery>
    fun getSalesmanId(sm_Id: Int, cu_Id: Int?) : LiveData<List<Delivery>>
    fun getById(dl_Id: Int): LiveData<Delivery>
    fun getItemByMasterId(dl_Id: Int): LiveData<List<Delivery_Items>>

    fun cancelJob()
}