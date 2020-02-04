package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Region (
    var rg_code: String?,
    var rg_parentId: Int?,
    var rg_description: String?,
    var rg_description_ar: String?,
    var rg_type: String?
){
    @PrimaryKey(autoGenerate = false)
    var rg_id:  Int = 0
}