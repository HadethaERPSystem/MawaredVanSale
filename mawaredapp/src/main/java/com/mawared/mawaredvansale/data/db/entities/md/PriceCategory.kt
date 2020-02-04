package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.PrimaryKey

class PriceCategory(
    var prc_code: String?,
    var prc_name: String?
) {
    @PrimaryKey(autoGenerate = false)
    var prc_Id: Int = 0
}