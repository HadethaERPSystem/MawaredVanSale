package com.mawared.mawaredvansale.data.db.dao.md

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.md.Product

@Dao
interface ProductDao: BaseDao<Product> {

    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Product")
    fun getAll(): LiveData<List<Product>>

    /**
     * Get single product data from the Data table.
     */
    @Query("SELECT * FROM Product WHERE pr_id=:id")
    fun getById(id: Int): LiveData<Product>

    /**
     * Get single product data from the Data table.
     */
    @Query("SELECT * FROM Product WHERE pr_barcode = :barcode LIMIT 1")
    fun getByBarcode(barcode: String): LiveData<Product>

    /**
     * Get single product data from the Data table.
     */
    @Query("SELECT * FROM Product WHERE pr_description LIKE :prod_name")
    fun getByTerm(prod_name: String): LiveData<List<Product>>
}