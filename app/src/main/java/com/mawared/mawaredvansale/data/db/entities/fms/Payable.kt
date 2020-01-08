package com.mawared.mawaredvansale.data.db.entities.fms

import androidx.room.Ignore
import androidx.room.PrimaryKey

data class Payable(
    var py_clientId: Int?,
    var py_orgId: Int?,
    var py_doc_no: Int?,
    var py_doc_date: String?,
    var py_vo_Id: Int?,
    var py_prefix: String?,
    var py_ref_no: String?,
    var py_sm_Id: Int?,
    var py_cu_Id: Int?,
    var py_amount_due: Double?,
    var py_amount: Double?,
    var py_change: Double?,
    var py_lc_amount: Double?,
    var py_lc_change: Double?,
    var py_cr_Id: Int?,
    var py_lc_cr_Id: Int?,
    var py_rate: Double?,
    var py_comment: String?,
    var py_isDeleted: Boolean?,
    var py_latitude: Double?,
    var py_longitude: Double?,
    var py_ref_Id: Int?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
) {
    @PrimaryKey(autoGenerate = false)
    var py_Id: Int = 0
    @Ignore
    var py_org_name: String? = null
    @Ignore
    var py_vo_name: String? = null
    @Ignore
    var py_vo_code: String? = null
    @Ignore
    var py_sm_name: String? = null
    @Ignore
    var py_cu_name: String? = null
    @Ignore
    var py_cr_symbol: String? = null
    var py_lc_cr_symbol: String? = null
    var py_cu_balance: Double? = null
}