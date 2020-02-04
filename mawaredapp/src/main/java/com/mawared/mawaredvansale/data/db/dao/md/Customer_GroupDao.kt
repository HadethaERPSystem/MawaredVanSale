package com.mawared.mawaredvansale.data.db.dao.md

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.md.Customer_Group

@Dao
interface Customer_GroupDao : BaseDao<Customer_Group> {

    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Customer_Group")
    fun getAll(): LiveData<List<Customer_Group>>

    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("SELECT * FROM Customer_Group WHERE cg_id = :id")
    fun getById(id: Int): LiveData<Customer_Group>
}