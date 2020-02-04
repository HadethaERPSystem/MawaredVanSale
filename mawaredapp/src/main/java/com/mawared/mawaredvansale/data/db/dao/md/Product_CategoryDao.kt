package com.mawared.mawaredvansale.data.db.dao.md

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.md.Product_Category

@Dao
interface Product_CategoryDao : BaseDao<Product_Category> {
    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Product_Category")
    fun getAll(): LiveData<List<Product_Category>>

    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("SELECT * FROM Product_Category WHERE pg_Id = :id")
    fun getById(id: Int): LiveData<Product_Category>
}