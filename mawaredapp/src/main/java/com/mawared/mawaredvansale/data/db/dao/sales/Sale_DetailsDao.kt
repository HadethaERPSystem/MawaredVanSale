package com.mawared.mawaredvansale.data.db.dao.sales

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mawared.mawaredvansale.data.db.dao.BaseDao
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Items

@Dao
interface Sale_DetailsDao : BaseDao<Sale_Items> {

    /**
     * Get all data from the Data table.
     */
    @Query("SELECT d.* " +
                 " FROM Sale_Items d " +
                 " LEFT JOIN Product p ON p.pr_Id = d.sld_prod_Id WHERE sld_sl_Id = :SaleId")
    fun getBySaleId(SaleId : Int): LiveData<List<Sale_Items>>

}