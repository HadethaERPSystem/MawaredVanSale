package com.mawared.mawaredvansale.data.db.dao.sales

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order

@Dao
interface Sale_OrderDao : BaseDao<Sale_Order>{

    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Sale_Order")
    fun getAll(): LiveData<List<Sale_Order>>

    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("SELECT * FROM Sale_Order WHERE so_id = :id")
    fun getById(id: Int): LiveData<Sale_Order>

    @Query("SELECT * FROM Sale_Order WHERE so_no = :docNo")
    fun getByNo(docNo : Int): LiveData<Sale_Order>
}