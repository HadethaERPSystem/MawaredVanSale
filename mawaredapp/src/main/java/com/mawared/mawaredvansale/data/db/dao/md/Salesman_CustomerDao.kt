package com.mawared.mawaredvansale.data.db.dao.md

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.md.Salesman_Customer

@Dao
interface Salesman_CustomerDao : BaseDao<Salesman_Customer>{

    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Salesman_Customer")
    fun getAll(): LiveData<List<Salesman_Customer>>

    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("SELECT * FROM Salesman_Customer WHERE sc_id = :id")
    fun getById(id: Int): LiveData<Salesman_Customer>
}