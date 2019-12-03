package com.mawared.mawaredvansale.data.db.dao.sales

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return

@Dao
interface Sale_ReturnDao : BaseDao<Sale_Return> {
    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Sale_Return")
    fun getAll(): LiveData<List<Sale_Return>>

    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("SELECT * FROM Sale_Return WHERE sr_id = :id")
    fun getById(id: Int): LiveData<Sale_Return>

    @Query("SELECT * FROM Sale_Return WHERE sr_doc_no = :docNo")
    fun getByNo(docNo : Int): LiveData<Sale_Return>
}