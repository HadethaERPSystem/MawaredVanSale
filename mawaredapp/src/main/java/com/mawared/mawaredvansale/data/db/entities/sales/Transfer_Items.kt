package com.mawared.mawaredvansale.data.db.entities.sales

import androidx.room.PrimaryKey

class Transfer_Items (
    var trd_tr_Id: Int?,
    var trd_rowNo: Int?,
    var trd_prod_Id: Int?,
    var trd_uom_Id: Int?,           // Unit of Measurement
    var trd_pack_qty: Double?,      // Pack Quantity
    var trd_pack_size: Double?,     // Pack Size
    var trd_unit_qty: Double?,      // Unit Quantity
    var created_at: String?,    // created datetime
    var created_by: String?,    // created user
    var updated_at: String?,    // Updated datetime
    var updated_by: String?     // Updated user
){
    @PrimaryKey(autoGenerate = false)
    var trd_Id: Int = 0

    var trd_uom_name: String? = null
    var trd_prod_name_ar: String? = null
    var trd_prod_name: String? = null
    var trd_barcode: String? = null

}