package com.mawared.mawaredvansale.data.db.entities.sales

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Sale_Items (
    var sld_sl_Id: Int?,            // Sale Id
    var sld_rowNo: Int?,            // Current Item Row Number
    var sld_ref_rowNo: Int?,       // Current item Sale Order Row Number
    var sld_prod_Id: Int?,          // Product Id

    var sld_uom_Id: Int?,           // Unit of Measurement

    var sld_pack_qty: Double?,      // Pack Quantity
    var sld_pack_size: Double?,     // Pack Size
    var sld_unit_qty: Double?,      // Unit Quantity

    var sld_unit_price: Double?,    // Unit Price
    var sld_line_total: Double?,    // Line Total
    var sld_dis_per: Double?,      // Percentage Discount
    var sld_dis_value: Double?,      // Fixed Amount Discount
    var sld_net_total: Double?,

    var sld_lotno: String?,         // Lot Number
    var sld_isPromotion: String?,   // Is Current Item Promotion Or Not : Y:Yes, N: No
    var sld_promotionId: Int?,      // Promotion Id
    var sld_warehouseId: Int?,   // Salesman Warehouse Identifier : Van
    var created_at: String?,    // created datetime
    var created_by: String?,    // created user
    var updated_at: String?,    // Updated datetime
    var updated_by: String?     // Updated user
){
    @PrimaryKey(autoGenerate = true)
    var sld_Id: Int = 0

    var sld_warehouse_name: String? = null
    var sld_uom_name: String? = null
    var sld_prod_name_ar: String? = null
    var sld_prod_name: String? = null
    var sld_barcode: String? = null
}
