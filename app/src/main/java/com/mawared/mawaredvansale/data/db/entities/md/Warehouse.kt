package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.PrimaryKey

class Warehouse(
    var wr_description: String?,
    var wr_description_ar: String?,
    var wr_code: String?
) {
    @PrimaryKey(autoGenerate = false)
    var wr_Id: Int = 0
}