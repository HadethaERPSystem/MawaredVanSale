package com.mawared.mawaredvansale.data.db.dao.md

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.md.Product_Price_List

@Dao
interface Product_Price_ListDao: BaseDao<Product_Price_List> {

    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Product_Price_List")
    fun getAll(): LiveData<List<Product_Price_List>>

    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("SELECT * FROM Product_Price_List WHERE pl_prod_Id = :id")
    fun getById(id: Int): LiveData<Product_Price_List>

    @Query("SELECT * FROM Product_Price_List WHERE pl_prod_Id = :prod_Id AND pl_currencyId = :cr_Id LIMIT 1")
    fun getItemPrice(prod_Id: Int, cr_Id: Int): LiveData<Product_Price_List>
}