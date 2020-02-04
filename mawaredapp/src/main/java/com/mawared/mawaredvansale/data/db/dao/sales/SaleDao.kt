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
    @Query("SELECT sl_id, sl_doc_no, sl_doc_date, sl_prefix, sl_refno, sl_clientId, sl_orgId, sl_customerId, cu.cu_name_ar as sl_customer_name," +
            "       h.sl_salesmanId, sm.sm_name as sl_salesman_name, 0 as sl_itemsno, h.sl_regionId, rg.rg_description_ar as sl_region_name, " +
            "       h.sl_total_amount, h.sl_net_amount, h.sl_currencyId, cr.cr_symb as sl_cur_symbol, sl_org_name, sl_vo_Id, sl_vo_name, sl_vo_code, sl_isDeleted, " +
            "       h.created_at, h.created_by," +
            "       h.updated_at, h.updated_by" +
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
    @Query("SELECT sl_id, sl_doc_no, sl_doc_date, sl_prefix, sl_refno, sl_clientId, sl_orgId, sl_customerId, cu.cu_name_ar as sl_customer_name," +
            "       h.sl_salesmanId, sm.sm_name as sl_salesman_name, 0 as sl_itemsno, h.sl_regionId, rg.rg_description_ar as sl_region_name, " +
            "       h.sl_total_amount, h.sl_net_amount, h.sl_currencyId, cr.cr_symb as sl_cur_symbol, sl_org_name, sl_vo_Id, sl_vo_name, sl_vo_code, sl_isDeleted, h.created_at, h.created_by," +
            "       h.updated_at, h.updated_by" +
            "  FROM Sale h" +
            "  LEFT JOIN Customer as cu ON cu.cu_id = h.sl_customerId" +
            "  LEFT JOIN Salesman as sm ON sm.sm_id = h.sl_salesmanId" +
            "  LEFT JOIN Region as rg ON rg.rg_id = h.sl_regionId" +
            "  LEFT JOIN Currency as cr ON cr.cr_id = h.sl_currencyId WHERE h.sl_id = :id")
    fun getById(id: Int): LiveData<Sale>

    @Query("SELECT sl_id, sl_doc_no, sl_doc_date, sl_prefix, sl_refno, sl_clientId, sl_orgId, sl_customerId, cu.cu_name_ar as sl_customer_name," +
            "       h.sl_salesmanId, sm.sm_name as sl_salesman_name, 0 as sl_itemsno, h.sl_regionId, rg.rg_description_ar as sl_region_name, " +
            "       h.sl_total_amount, h.sl_net_amount, h.sl_currencyId, cr.cr_symb as sl_cur_symbol, sl_org_name, sl_vo_Id, sl_vo_name, sl_vo_code, sl_isDeleted, h.created_at, h.created_by," +
            "       h.updated_at, h.updated_by" +
            "  FROM Sale h" +
            "  LEFT JOIN Customer as cu ON cu.cu_id = h.sl_customerId" +
            "  LEFT JOIN Salesman as sm ON sm.sm_id = h.sl_salesmanId" +
            "  LEFT JOIN Region as rg ON rg.rg_id = h.sl_regionId" +
            "  LEFT JOIN Currency as cr ON cr.cr_id = h.sl_currencyId WHERE sl_doc_no = :docNo")
    fun getByNo(docNo : Int): LiveData<Sale>
}