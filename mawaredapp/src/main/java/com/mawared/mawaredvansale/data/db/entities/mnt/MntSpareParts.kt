package com.mawared.mawaredvansale.data.db.entities.mnt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MntSpareParts(
    var client_id: Int?,
    var org_id: Int?,
    var mnt_trns_id: Int?,
    var is_main: Boolean?,
    var whs_id: Int?,
    var loc_id: Int?,
    var prod_id: Int?,
    var unit_price: Double?,
    var disc_prcnt: Double?,
    var dis_value: Double?,
    var uom_id: Int?,
    var pack_size: Double?,
    var pqty: Double?,
    var qty: Double?,
    var amount: Double?,
    var line_total: Double?,
    var net_total: Double?,
    var is_gift: Boolean?,
    var batch_no: String?,
    var expiry_date: String?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
)
{
    @PrimaryKey(autoGenerate = true)
    var mntSpPartId:  Int = 0
    var whs_name: String? = null
    var loc_name: String? = null
    var prod_name: String? = null
    var prod_name_ar: String? = null
    var barcode: String? = null
    var partNo: String? = null
    var uom_name: String? = null
}