package com.mawared.mawaredvansale.data.db.entities.inventory

import androidx.room.Ignore
import androidx.room.PrimaryKey

data class Stockin_Items(
    var lineNum: Int?,
    var docEntry: Int?,
    var prodId: Int?,
    var prod_name: String?,
    var uomEntry: Int?,
    var uomName: String?,
    var qty: Double?,
    var pack_size: Double?,     // Pack Size
    var invQty: Double?,
    var orderQty: Double?,
    var locId: Int?,
    var locName: String?,
    var picker_Id: Int?,
    var isGift: Boolean?,
    var baseRef: String?,
    var baseType: Int?,
    var baseEntry: Int?,
    var baseLine: Int?,
    var unitCost: Double?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
) {
    @PrimaryKey(autoGenerate = false)
    var docItemEntry: Int = 0
    var picker_name: String? = null
    var barcode: String? = null
}