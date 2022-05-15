package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UnitConvertion (
    var uom_from: Int?,
    var uom_from_name: String?,
    var uom_from_code: String?,
    var qty2: Double?,
    var uom: Int?,
    var uom_name: String?,
    var uom_code: String?,
    var qty: Double?,
    var conv_rate: Double?
){
    @PrimaryKey(autoGenerate = false)
    var uc_Id:  Int = 0
}