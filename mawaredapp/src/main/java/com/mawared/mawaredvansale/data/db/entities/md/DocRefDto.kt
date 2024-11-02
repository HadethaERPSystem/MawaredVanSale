package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.PrimaryKey

class DocRefDto (
    var ref_Id: Int?,
    var ref_no: String?,
    var ref_type: Int?,
    var discPrcnt: Double?,
    var netAmount: Double?
)