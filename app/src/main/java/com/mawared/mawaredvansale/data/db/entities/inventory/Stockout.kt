package com.mawared.mawaredvansale.data.db.entities.inventory

import androidx.room.Ignore
import androidx.room.PrimaryKey

data class Stockout(
    var sot_clientId: Int?,         // client Id
    var sot_orgId: Int?,            // organization Id

    var sot_doc_no: Int?,
    var sot_doc_date: String?,
    var sot_prefix: String?,
    var sot_vo_Id: Int?,

    var sot_entry_Id: Int?,

    var sot_inv_status: String?,

    var sot_warehouse_Id: Int?,

    var sot_isDeleted: Boolean?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
) {
    @PrimaryKey(autoGenerate = false)
    var sot_Id: Int = 0
    var sot_itemsno = 0
    var sot_entry_refno: String?= null
    @Ignore
    var sot_org_name: String? = null
    @Ignore
    var sot_vo_name: String?=null
    @Ignore
    var sot_ref_no: String? = null
    @Ignore
    var sot_inv_status_name: String? = null
    @Ignore
    var sot_warehouse_name: String? = null
}