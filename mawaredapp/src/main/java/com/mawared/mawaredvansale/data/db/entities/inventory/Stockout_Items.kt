package com.mawared.mawaredvansale.data.db.entities.inventory

import androidx.room.Ignore
import androidx.room.PrimaryKey

data class Stockout_Items(
    var soti_sot_Id: Int?,
    var soti_rowNo: Int?,
    var soti_prod_Id: Int?,
    var soti_uom_Id: Int?,
    var soti_pack_qty: Double?,
    var soti_pack_size: Double?,     // Pack Size
    var soti_unit_qty: Double?,
    var soti_loc_Id: Int?,
    var soti_picker_Id: Int?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user

) {
    @PrimaryKey(autoGenerate = false)
    var soti_Id: Int = 0
    @Ignore
    var soti_prod_name: String? = null
    @Ignore
    var soti_barcode: String? = null
    @Ignore
    var soti_uom_name: String? = null
    @Ignore
    var soti_loc_name: String? = null
    @Ignore
    var soti_picker_name: String? = null
}