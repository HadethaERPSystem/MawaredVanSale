package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Product_Category(
    var pg_code: String?,           // Category Code
    var pg_clientId: Int?,          // Client Identifier
    var pg_description: String?,    // Category Latin Name
    var pg_description_ar: String?  // Category Arabic name
) {
    @PrimaryKey(autoGenerate = false)
    var pg_Id:  Int = 0
}