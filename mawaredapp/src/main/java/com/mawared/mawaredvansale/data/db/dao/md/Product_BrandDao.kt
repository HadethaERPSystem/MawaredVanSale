package com.mawared.mawaredvansale.data.db.dao.md

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.md.Product_Brand

@Dao
interface Product_BrandDao : BaseDao<Product_Brand>{
    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Product_Brand")
    fun getAll(): LiveData<List<Product_Brand>>

    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("SELECT * FROM Product_Brand WHERE br_Id = :id")
    fun getById(id: Int): LiveData<Product_Brand>
}