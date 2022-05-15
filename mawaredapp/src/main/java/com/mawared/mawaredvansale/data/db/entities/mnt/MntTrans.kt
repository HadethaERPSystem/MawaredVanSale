package com.mawared.mawaredvansale.data.db.entities.mnt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MntTrans(
    var client_id: Int?,
    var org_id: Int?,
    var mnt_id: Int?,
    var doc_no: Long?,
    var cust_id: Int?,
    var firstDesc: String?,
    var finalDesc: String?,
    var workCost: Double?,
    var lineCost: Double?,
    var totalCost: Double?,
    var mntTypeId: Int?,
    var mntStatusId: Int?,
    var is_free: Boolean?,
    var is_free_serv: Boolean?,
    var price_cat_id: Int?,
    var uom_id: Int?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
) {
    @PrimaryKey(autoGenerate = true)
    var mnt_trans_id:  Int = 0
    var cust_name: String? = null
    var mntTypeName: String? = null
    var mntStatusName: String? = null
    var price_cat_code: String? = null
    var price_cat_name: String? = null
}