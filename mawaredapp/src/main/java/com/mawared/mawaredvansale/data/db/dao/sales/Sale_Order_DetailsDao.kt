package com.mawared.mawaredvansale.data.db.dao.sales

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order_Items

@Dao
interface Sale_Order_DetailsDao : BaseDao<Sale_Order_Items>{
    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Sale_Order_Items WHERE sod_so_Id = :so_id")
    fun getBySaleId(so_id : Int): LiveData<List<Sale_Order_Items>>
}