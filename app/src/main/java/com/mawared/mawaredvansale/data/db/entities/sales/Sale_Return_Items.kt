package com.mawared.mawaredvansale.data.db.entities.sales

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Sale_Return_Items(
    var srd_sr_Id: Int?,
    var srd_rowNo: Int?,            // Current Item Row Number
    var srd_prod_Id: Int?,          // Product Id
    var srd_uom_Id: Int?,           // Unit of Measurement
    var srd_pack_qty: Double?,      // Pack Quantity
    var srd_pack_size: Double?,     // Pack Size
    var srd_unit_qty: Double?,      // Unit Quantity
    var srd_unit_price: Double?,    // Unit Price
    var srd_line_total: Double?,    // Line Total
    var srd_dis_per: Double?,      // Percentage Discount
    var srd_dis_value: Double?,      // Fixed Amount Discount
    var srd_net_total: Double?,    // Line Total
    var srd_lotno: String?,         // Lot Number
    var srd_isPromotion: String?,   // Is Current Item Promotion Or Not : Y:Yes, N: No
    var srd_promotionId: Int?,      // Promotion Id
    var srd_warehouseId: Int?,       // Salesman Warehouse Id
    var srd_ref_rowNo: Int?,
    var created_at: String?,    // created datetime
    var created_by: String?,    // created user
    var updated_at: String?,    // Updated datetime
    var updated_by: String?     // Updated user
) {
    @PrimaryKey(autoGenerate = true)
    var srd_Id: Int = 0
    @Ignore
    var srd_warehouse_name: String? = null
    @Ignore
    var srd_prod_name: String? = null
    @Ignore
    var srd_barcode: String? = null
    @Ignore
    var srd_uom_name: String? = null
}