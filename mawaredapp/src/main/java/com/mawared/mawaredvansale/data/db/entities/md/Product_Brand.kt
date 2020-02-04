package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Product_Brand(
    var br_code: String?,           //Brand code name
    var br_clientId: Int?,          // Client Identifier
    var br_description: String?,    // Brand Latin Name
    var br_description_ar: String?  // Brand Arabic Name
) {
    @PrimaryKey(autoGenerate = false)
    var br_Id:  Int = 0
}