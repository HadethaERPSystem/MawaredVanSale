package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Currency(
   var cr_code: String?,
   var cr_description: String?,
   var cr_symb: String?
){
    @PrimaryKey(autoGenerate = false)
    var cr_id:  Int = 0
}