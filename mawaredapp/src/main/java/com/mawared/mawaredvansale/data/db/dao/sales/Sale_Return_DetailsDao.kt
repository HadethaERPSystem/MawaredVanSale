package com.mawared.mawaredvansale.data.db.dao.sales

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return_Items

@Dao
interface Sale_Return_DetailsDao : BaseDao<Sale_Return_Items> {
    /**
     * Get all data from the Data table.
     */
    @Query("SELECT * FROM Sale_Return_Items WHERE srd_sr_Id = :sr_id")
    fun getBySaleId(sr_id : Int): LiveData<List<Sale_Return_Items>>
}