package com.mawared.mawaredvansale.services.repositories.fms

import androidx.lifecycle.LiveData
import com.mawared.mawaredvansale.data.db.entities.fms.Payable

interface IPayableRepository {
    fun insert(baseEo: Payable) : LiveData<Payable>
    fun getPayable(sm_Id: Int, cu_Id: Int?) : LiveData<List<Payable>>
    fun getById(py_Id: Int): LiveData<Payable>
    fun delete(py_Id: Int): LiveData<String>

    fun cancelJob()
}