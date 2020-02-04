package com.mawared.mawaredvansale.services.repositories.salereturn

import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return_Items
import com.mawared.mawaredvansale.services.netwrok.responses.ResponseSingle

interface ISaleReturnRepository {
    fun insert(baseEo: Sale_Return) : LiveData<Sale_Return>
    suspend fun SaveOrUpdate(baseEo: Sale_Return) : ResponseSingle<Sale_Return>
    fun getSaleReturn(sm_Id: Int, cu_Id: Int?) : LiveData<List<Sale_Return>>
    fun getReturnById(sr_Id: Int): LiveData<Sale_Return>
    fun delete(sr_Id: Int): LiveData<String>
    fun getItemBySaleReturnId(sr_Id: Int): LiveData<List<Sale_Return_Items>>

    fun cancelJob()
}