package com.mawared.mawaredvansale.data.db.entities.inventory

import androidx.room.Ignore
import androidx.room.PrimaryKey

data class Stockin_Items(
    var sini_rowNo: Int?,
    var sini_sin_Id: Int?,
    var sini_prod_Id: Int?,
    var sini_uom_Id: Int?,
    var sini_pack_qty: Double?,
    var sini_pack_size: Double?,     // Pack Size
    var sini_unit_qty: Double?,
    var sini_loc_Id: Int?,
    var sini_picker_Id: Int?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
) {
    @PrimaryKey(autoGenerate = false)
    var sin_Id: Int = 0
    @Ignore
    var sini_prod_name: String? = null
    @Ignore
    var sini_barcode: String? = null
    @Ignore
    var sini_uom_name: String? = null
    @Ignore
    var sini_loc_name: String?=null
    @Ignore
    var sini_picker_name: String? = null
}