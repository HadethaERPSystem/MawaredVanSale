package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.PrimaryKey

data class Customer_Payment_Type(
    var cpt_name_ar: String,
    var cpt_name: String?,
    var cpt_code: String
) {
    @PrimaryKey(autoGenerate = false)
    var cpt_Id: Int = 0
}