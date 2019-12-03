package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity

@Entity
data class Voucher (
    var vo_Id: Int,
    var vo_prefix: String?,
    var vo_code: String?,
    var vo_name: String?,
    var vo_name_ar: String?
)