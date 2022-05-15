package com.mawared.mawaredvansale.data.db.entities.mnt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MntServ(
    var client_id: Int?,
    var org_id: Int?,
    var mnt_trns_id: Int?,
    var is_main: Boolean?,
    var serv_id: Int?,
    var uom_id: Int?,
    var unit_price: Double?,
    var qty : Double?,
    var amount : Double?,
    var is_cancel: Boolean?,
    var is_gift: Boolean?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
)
{
    @PrimaryKey(autoGenerate = true)
    var mtnSerId:  Int = 0
    var serv_name: String? = null
    var uom_name: String? = null
}