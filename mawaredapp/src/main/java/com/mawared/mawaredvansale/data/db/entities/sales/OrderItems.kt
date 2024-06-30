package com.mawared.mawaredvansale.data.db.entities.sales

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OrderItems (
    @PrimaryKey
    var od_Id: Int = 0,
    var od_rowNo: Int?,
    var od_prod_Id: Int?,
    var od_prod_name: String?,
    var od_uom_Id: Int?,
    var od_uom_name: String?,
    var od_pack_qty: Double?,      // Pack Quantity
    var od_pack_size: Double?,     // Pack Size
    var od_unit_qty: Double?,      // Unit Quantity
    var od_gift_qty: Double? = null,
    var od_unit_price: Double?,    // Unit Price
    var od_price_afd: Double?,     // Unit Price After discount
    var od_line_total: Double?,    // Line Total
    var od_discount: Double?,      // Percentage Discount
    var od_disvalue: Double?,      // Fixed Amount Discount
    var od_add_dis_per: Double?,
    var od_add_dis_value: Double?,
    var od_disc_amnt: Double?,
    var od_net_total: Double?,
    var od_wr_Id: Int?,
    var od_wr_name: String?,
    var od_batch_no: String?,
    var od_expiry_date: String?,
    var od_mfg_date:String?,
    var od_lotno: String?,         // Lot Number
    var od_isPromotion: String?,   // Is Current Item Promotion Or Not : Y:Yes, N: No
    var od_promotionId: Int?,      // Promotion Id
    var od_isGift: Boolean,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
)