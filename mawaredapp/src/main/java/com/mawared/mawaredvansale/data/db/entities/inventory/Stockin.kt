package com.mawared.mawaredvansale.data.db.entities.inventory

import androidx.room.Ignore
import androidx.room.PrimaryKey

data class Stockin(
    var sin_clientId: Int?,         // client Id
    var sin_orgId: Int?,            // organization Id
    @Ignore
    var sin_org_name: String,
    var sin_doc_no: Int?,
    var sin_doc_date: String?,
    var sin_prefix: String?,
    var sin_vo_Id: Int?,
    @Ignore
    var sin_vo_name: String?,
    var sin_entry_Id: Int?,
    var sin_entry_refno: String?,
    @Ignore
    var sin_ref_no: String?,
    var sin_inv_status: String?,
    @Ignore
    var sin_inv_status_name: String?,
    var sin_warehouse_Id: Int?,
    @Ignore
    var sin_warehouse_name: String?,
    var sin_isDeleted: Boolean?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
) {
    @PrimaryKey(autoGenerate = false)
    var sin_Id: Int = 0
    var sin_itemsno = 0
}