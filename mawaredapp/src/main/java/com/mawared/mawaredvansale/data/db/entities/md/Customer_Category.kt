package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Customer_Category(
    var cat_code: String?,
    var cat_description: String?,
    var cat_description_ar: String?
){
    @PrimaryKey(autoGenerate = false)
    var cat_Id:  Int = 0
}