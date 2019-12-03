package com.mawared.mawaredvansale.data.db.dao.sales

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.sales.Due_Invoice

@Dao
interface Due_InvoiceDao : BaseDao<Due_Invoice> {

    @Query("SELECT * FROM Due_Invoice WHERE di_customerId = :customerId")
    fun getByCustomer(customerId : Int): LiveData<Due_Invoice>
}