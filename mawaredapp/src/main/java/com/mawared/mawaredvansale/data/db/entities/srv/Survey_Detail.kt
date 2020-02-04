package com.mawared.mawaredvansale.data.db.entities.srv

import androidx.room.PrimaryKey

class Survey_Detail(
    var srvd_srv_Id: Int?,
    var srvd_Question: String?,
    var srvd_Answer: String?,
    var srvd_wight: Int?,
    var srvd_qs_type: Int?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
) {
    @PrimaryKey(autoGenerate = false)
    var srvd_Id: Int = 0
}