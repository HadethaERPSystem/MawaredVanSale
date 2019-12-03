package com.mawared.mawaredvansale.data.db.dao.md

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.md.Customer

@Dao
interface CustomerDao : BaseDao<Customer>{

    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Customer")
    fun getAll(): LiveData<List<Customer>>

    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("SELECT * FROM Customer WHERE cu_id = :id")
    fun getById(id: Int): LiveData<Customer>

    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("SELECT * FROM Customer WHERE cu_name LIKE :term")
    fun getByTerm(term: String): LiveData<List<Customer>>

    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("DELETE FROM Customer")
    fun deleteAll()

}