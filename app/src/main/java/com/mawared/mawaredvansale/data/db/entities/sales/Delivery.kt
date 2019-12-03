package com.mawared.mawaredvansale.data.db.entities.sales

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDateTime

@Entity
data class Delivery(
    var dl_clientId: Int?,
    var dl_orgId: Int?,
    var dl_doc_date: String?,
    var dl_doc_no:  Int?,
    var dl_refNo: String?,
    var dl_refId: Int?,
    var dl_salesmanId: Int?,
    var dl_warehouseId: Int?,
    var dl_customerId: Int?,
    var dl_rg_Id: Int?,
    var dl_isDeleted: Boolean?,
    var dl_comment: String?,
    var dl_isDelivered: String?,      // Is Delivered : Y: Yes, N: No, W:Waiting
    var dl_total_amount: Double?,
    var dl_net_amount: Double?,
    var dl_rate: Double?,
    var dl_latitude: Double?,
    var dl_longitude: Double?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
) {
    @PrimaryKey(autoGenerate = false)
    var dl_Id:  Int = 0
    var items: ArrayList<Delivery_Items> = arrayListOf()
    var dl_customer_name: String?  = null
    var dl_warehouse_name: String? = null
    var dl_salesman_name: String? = null
    var dl_org_name: String? = null
    var dl_region_name: String? = null
}