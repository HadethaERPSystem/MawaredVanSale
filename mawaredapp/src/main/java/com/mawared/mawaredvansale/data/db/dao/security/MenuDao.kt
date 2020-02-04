package com.mawared.mawaredvansale.data.db.dao.security

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.security.Menu


@Dao
interface MenuDao : BaseDao<Menu> {


    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Menu")
    fun getAll(): LiveData<List<Menu>>
}