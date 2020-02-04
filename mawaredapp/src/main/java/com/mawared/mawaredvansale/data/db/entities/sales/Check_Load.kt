package com.mawared.mawaredvansale.data.db.entities.sales

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDateTime

@Entity
data class Check_Load(
    var cl_clientId: Int?,      // Client Identifier
    var cl_orgId: Int?,         // Branch Id
    @Ignore
    var cl_org_name: String?,
    var cl_salesmanId: Int?,    // Salesman Identifier
    @Ignore
    var cl_salesman_name: String?,
    var cl_doc_date: String?,// Transfer and Check-Load Date
    var cl_doc_no: Int?,        // Check Load Number
    var cl_refno: String?,      // Reference Number
    var cl_vo_Id: Int?,
    @Ignore
    var cl_vo_name: String?,
    @Ignore
    var cl_vo_code: String?,
    var cl_warehouseId: Int?,   // Salesman Warehouse Identifier : Van
    @Ignore
    var cl_warehouse_name: String?,
    var cl_refId: Int?,    // Transfer reference identifier
    var cl_isDeleted: Boolean?,
    var cl_comment: String?,    // Transfer comment
    var cl_done: String?        // Done: Y:Yes, N: No, W: Waiting
) {
    @PrimaryKey(autoGenerate = false)
    var cl_Id:  Int = 0
}
