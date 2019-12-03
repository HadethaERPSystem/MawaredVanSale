package com.mawared.mawaredvansale.data.db.dao.md

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.md.Region

@Dao
interface RegionDao : BaseDao<Region> {
    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Region")
    fun getAll(): LiveData<List<Region>>

    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("SELECT * FROM Region WHERE rg_id = :id")
    fun getById(id: Int): LiveData<Region>
}