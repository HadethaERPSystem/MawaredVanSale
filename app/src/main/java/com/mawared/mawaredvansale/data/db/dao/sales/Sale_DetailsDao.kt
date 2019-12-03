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
    @Query("SELECT d.sld_id, d.sld_sl_Id, d.sld_rowNo, d.sld_ref_rowNo, d.sld_prod_Id, p.pr_description sld_prod_name, p.pr_barcode sld_barcode," +
                 "       d.sld_uom_Id, '' as sld_uom_name, d.sld_pack_qty, d.sld_pack_size, d.sld_unit_qty, d.sld_unit_price, d.sld_line_total, d.sld_dis_per, d.sld_dis_value," +
                 "       ((d.sld_unit_price * d.sld_pack_qty) - d.sld_dis_value) sld_net_total, d.sld_lotno, d.sld_isPromotion, d.sld_promotionId, d.sld_warehouseId, d.created_at, d.created_by," +
                 "       d.updated_at, d.updated_by " +
                 " FROM Sale_Items d " +
                 " LEFT JOIN Product p ON p.pr_Id = d.sld_prod_Id WHERE sld_sl_Id = :SaleId")
    fun getBySaleId(SaleId : Int): LiveData<List<Sale_Items>>

}