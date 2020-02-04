package com.mawared.mawaredvansale.data.db.entities.sales

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate

@Entity
data class Check_Load_Details(
    var cld_rowNo: Int?,
    var cld_cl_Id: Int?,        // Check-Load Id
    var cld_prod_Id: Int?,        // product Id
    @Ignore
    var cld_prod_name:String?,
    var cld_pack_qty: Double?,  // Pack Quantity
    var cld_unit_qty: Double?,  // Unit Quantity
    var cld_lotno:String?,      //Lot Number
    var cld_expiry_date: String?, // Expiry Date
    var cld_uom_Id: Int?,        // Unit of Measurement
    @Ignore
    var cld_uom_name: String
) {
    @PrimaryKey(autoGenerate = false)
    var cld_Id:  Int = 0
}