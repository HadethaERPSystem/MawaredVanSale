package com.mawared.mawaredvansale.data.db.dao.md

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.md.Call_Cycle

@Dao
interface Call_CycleDao : BaseDao<Call_Cycle> {

    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Call_Cycle")
    fun getAll(): LiveData<List<Call_Cycle>>

    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("SELECT * FROM Call_Cycle WHERE cy_id = :id")
    fun getById(id: Int): LiveData<Call_Cycle>
}