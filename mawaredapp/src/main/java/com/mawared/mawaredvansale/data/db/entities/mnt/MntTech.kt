package com.mawared.mawaredvansale.data.db.entities.mnt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MntTech(
    var client_id: Int?,
    var org_id: Int?,
    var mnt_trns_id: Int?,
    var tech_id: Int?,
    var techCode: String?,
    var timeTaken: String?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
) {
    @PrimaryKey(autoGenerate = true)
    var mntTechId:  Int = 0
    var tech_name: String? = null
}