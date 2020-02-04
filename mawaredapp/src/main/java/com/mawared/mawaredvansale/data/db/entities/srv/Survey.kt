package com.mawared.mawaredvansale.data.db.entities.srv

import androidx.room.PrimaryKey

class Survey(
    var srv_name: String?,
    var srv_cu_Id: Int?,
    var srv_vst_no: Int?,
    var srv_vst_date: String?,
    var srv_prefix: String?,
    var srv_ref_no: String?,
    var srv_vst_type_Id: Int?,
    var srv_next_vst_date: String?,
    var srv_clientId: Int?,
    var srv_orgId: Int?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
) {
    @PrimaryKey(autoGenerate = false)
    var srv_Id: Int = 0
    var srv_cu_name: String? = null
    var srv_vst_type_name: String? = null
    var items: ArrayList<Survey_Detail> = arrayListOf()
}