package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Customer_Group(
    var cg_code: String?,
    var cg_description: String?,
    var cg_description_ar: String?
){
    @PrimaryKey(autoGenerate = false)
    var cg_Id:  Int = 0
}