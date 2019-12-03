package com.mawared.mawaredvansale.data.db.dao.md

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.md.Customer_Price_List

@Dao
interface Customer_Price_ListDao : BaseDao<Customer_Price_List> {

    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Customer_Price_List")
    fun getAll(): LiveData<List<Customer_Price_List>>

    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("SELECT * FROM Customer_Price_List WHERE cpl_id = :id")
    fun getById(id: Int): LiveData<Customer_Price_List>
}