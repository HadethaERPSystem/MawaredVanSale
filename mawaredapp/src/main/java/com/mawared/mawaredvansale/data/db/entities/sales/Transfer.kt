package com.mawared.mawaredvansale.data.db.entities.sales

import androidx.room.Ignore
import androidx.room.PrimaryKey

class Transfer(
    var tr_clientId: Int?,
    var tr_orgId: Int?,
    var tr_doc_no: Int?,
    var tr_doc_date: String?,
    var tr_vo_Id: Int?,
    var tr_prefix: String?,
    var tr_ref_no: String?,
    var tr_wr_Id: Int?,
    var tr_notes: String?,
    var tr_isDeleted: Boolean?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
) {
    @PrimaryKey(autoGenerate = false)
    var tr_Id: Int = 0
    var tr_org_name: String? = null
    var tr_vo_code: String? = null
    var tr_vo_name: String? = null
    var tr_wr_name: String? = null

    @Ignore
    var items: ArrayList<Transfer_Items> = arrayListOf()
    @Ignore
    var items_deleted: ArrayList<Transfer_Items> = arrayListOf()}