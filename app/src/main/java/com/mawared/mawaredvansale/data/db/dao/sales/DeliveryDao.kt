package com.mawared.mawaredvansale.data.db.dao.sales

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery

@Dao
interface DeliveryDao : BaseDao<Delivery>{
    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Delivery")
    fun getAll(): LiveData<List<Delivery>>

    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("SELECT * FROM Delivery WHERE dl_Id = :id")
    fun getById(id: Int): LiveData<Delivery>

    @Query("SELECT * FROM Delivery WHERE dl_doc_no = :docNo")
    fun getByNo(docNo : Int): LiveData<Delivery>
}