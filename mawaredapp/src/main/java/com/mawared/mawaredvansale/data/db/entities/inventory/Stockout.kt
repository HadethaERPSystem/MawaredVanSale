package com.mawared.mawaredvansale.data.db.entities.inventory

import androidx.room.Ignore
import androidx.room.PrimaryKey

data class Stockout(
    var doc_clientId: Int?,         // client Id
    var doc_orgId: Int?,            // organization Id

    var doc_no: Int?,
    var doc_date: String?,
    var prefix: String?,
    var vo_Id: Int?,
    var vo_name: String?,
    var vo_code: String?,
    var bp_Id: Int?,
    var baseDocEntry: Int?,
    var baseRefno: String?,
    var invStatus: String?,
    var whsId: Int?,
    var whsName: String?,
    var isDeleted: Boolean?,
    var notes: String?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
) {
    @PrimaryKey(autoGenerate = false)
    var docEntry: Int = 0
    var org_name: String? = null
    var refNo: String? = null
    var bpName: String? = null
    var invStatusName: String? = null
    @Ignore
    var docLines: ArrayList<Stockout_Items> = arrayListOf()
    @Ignore
    var docDelLines: ArrayList<Stockout_Items> = arrayListOf()
}