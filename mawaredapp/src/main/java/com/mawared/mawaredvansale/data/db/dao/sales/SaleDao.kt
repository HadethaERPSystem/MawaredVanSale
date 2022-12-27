package com.mawared.mawaredvansale.data.db.dao.sales

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.sales.Sale

@Dao
interface SaleDao : BaseDao<Sale> {

    /**
     * Get all data from the Data table.
     */
    @Query("SELECT h.*" +
            "  FROM Sale h" +
            "  LEFT JOIN Customer as cu ON cu.cu_id = h.sl_customerId" +
            "  LEFT JOIN Salesman as sm ON sm.sm_id = h.sl_salesmanId" +
            "  LEFT JOIN Region as rg ON rg.rg_id = h.sl_regionId" +
            "  LEFT JOIN Currency as cr ON cr.cr_id = h.sl_currencyId")
    fun getAll(): LiveData<List<Sale>>

    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("SELECT h.*" +
            "  FROM Sale h " +
            "  LEFT JOIN Customer as cu ON cu.cu_id = h.sl_customerId" +
            "  LEFT JOIN Salesman as sm ON sm.sm_id = h.sl_salesmanId" +
            "  LEFT JOIN Region as rg ON rg.rg_id = h.sl_regionId" +
            "  LEFT JOIN Currency as cr ON cr.cr_id = h.sl_currencyId WHERE h.sl_id = :id")
    fun getById(id: Int): LiveData<Sale>

    @Query("SELECT h.*" +
            "  FROM Sale h" +
            "  LEFT JOIN Customer as cu ON cu.cu_id = h.sl_customerId" +
            "  LEFT JOIN Salesman as sm ON sm.sm_id = h.sl_salesmanId" +
            "  LEFT JOIN Region as rg ON rg.rg_id = h.sl_regionId" +
            "  LEFT JOIN Currency as cr ON cr.cr_id = h.sl_currencyId WHERE sl_doc_no = :docNo")
    fun getByNo(docNo : Int): LiveData<Sale>
}