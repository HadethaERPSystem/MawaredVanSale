package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDateTime

@Entity
data class Currency_Rate (
    var cr_code_from: String?,
    var cr_code_to: String?,
    var cr_rate: Double?,
    var cr_date: String?
){
    @PrimaryKey(autoGenerate = false)
    var cur_id:  Int = 0
}
