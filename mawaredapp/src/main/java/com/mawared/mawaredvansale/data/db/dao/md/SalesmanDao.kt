package com.mawared.mawaredvansale.data.db.dao.md

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.md.Salesman

@Dao
interface SalesmanDao : BaseDao<Salesman>{

    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Salesman")
    fun getAll(): LiveData<List<Salesman>>

    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("SELECT * FROM Salesman WHERE sm_Id = :id")
    fun getById(id: Int): LiveData<Salesman>
}