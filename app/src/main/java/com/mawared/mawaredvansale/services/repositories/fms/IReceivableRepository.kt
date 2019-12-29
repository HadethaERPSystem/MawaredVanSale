package com.mawared.mawaredvansale.services.repositories.fms

import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.services.netwrok.responses.SingleRecResponse

interface IReceivableRepository {
    fun insert(baseEo: Receivable) : LiveData<Receivable>
    suspend fun SaveOrUpdate(baseEo: Receivable) : SingleRecResponse<Receivable>
    fun getReceivable(sm_Id: Int, cu_Id: Int?) : LiveData<List<Receivable>>
    fun getById(rcv_Id: Int): LiveData<Receivable>
    fun delete(rcv_Id: Int): LiveData<String>
    
    fun cancelJob()
}