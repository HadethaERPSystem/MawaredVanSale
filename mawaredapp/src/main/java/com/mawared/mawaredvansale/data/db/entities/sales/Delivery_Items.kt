package com.mawared.mawaredvansale.data.db.entities.sales

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate

@Entity
data class Delivery_Items(
    var dld_dl_Id: Int?,        //Transfer or delivery header Id
    var dld_prod_Id: Int?,        //Product Id
    var dld_pack_qty: Double?,  //Pack Quantity
    var dld_pack_size: Double?,
    var dld_unit_qty: Double?,  //Unit Quantity
    var dld_unit_price: Double?,//Unit Price
    var dld_pack_price: Double?,//Pack Price
    var dld_line_total: Double?,
    var dld_dis_per: Double?,  // Discount Percentage
    var dld_dis_value: Double?,  // Discount Value
    var dld_net_total: Double?,
    var dld_isDeliverd: Boolean?,
    var dld_qty: Double?,
    var dld_lotno: String?,     //Lot Number
    var dld_batch_no: String?,
    var dld_expiry_date: String?,    // Expiry date
    var dld_uom_Id: Int?        //Unit Of Measurement
) {
    @PrimaryKey(autoGenerate = false)
    var dld_Id:  Int = 0
    var dld_prod_name: String? = null
    var dld_barcode: String? = null
    var dld_part_no: String? = null
    var dld_uom_name: String? = null
    var dld_uom_desc: String? = null
}