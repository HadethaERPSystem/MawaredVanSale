package com.mawared.mawaredvansale.services.repositories.transfer

import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer_Items

interface ITransferRepository {
    fun saveOrUpdate(baseEo: Transfer) : LiveData<Transfer>
    fun getByUserId(userId: Int) : LiveData<List<Transfer>>
    fun getById(tr_Id: Int): LiveData<Transfer>
    //fun delete(sr_Id: Int): LiveData<String>
    fun getItemsByMasterId(tr_Id: Int): LiveData<List<Transfer_Items>>

    fun cancelJob()
}