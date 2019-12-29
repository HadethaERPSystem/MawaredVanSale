package com.mawared.mawaredvansale.data.db.entities.fms

import androidx.room.Ignore
import androidx.room.PrimaryKey

data class Receivable(
    var rcv_clientId: Int?,
    var rcv_orgId: Int?,
    var rcv_doc_no: Int?,
    var rcv_doc_date: String?,
    var rcv_vo_Id: Int?,
    var rcv_prefix: String?,
    var rcv_ref_no: String?,
    var rcv_sm_Id: Int?,
    var rcv_cu_Id: Int?,
    var rcv_amount_due: Double?,
    var rcv_amount: Double?,
    var rcv_change: Double?,
    var rcv_lc_amount: Double?,
    var rcv_lc_change: Double?,
    var rcv_cr_Id: Int?,
    var rcv_lc_cr_Id: Int?,
    var rcv_rate: Double?,
    var rcv_comment: String?,
    var rcv_isDeleted: Boolean?,
    var rcv_latitude: Double?,
    var rcv_longitude: Double?,
    var rcv_ref_Id: Int?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
) {
    @PrimaryKey(autoGenerate = false)
    var rcv_Id: Int = 0
    var rcv_org_name: String? = null
    var rcv_vo_name: String? = null
    var rcv_vo_code: String? = null
    var rcv_sm_name: String? = null
    var rcv_cu_name: String? = null
    var rcv_cr_symbol: String? = null
    var rcv_lc_cr_symbol: String? = null
}