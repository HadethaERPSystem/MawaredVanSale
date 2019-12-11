package com.mawared.mawaredvansale.data.db.entities.security

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Menu (
    var order:Int?,
    var menu_name: String,
    var menu_code: String,
    val icon: Int
){
    @PrimaryKey(autoGenerate = false)
    var menu_id: Int = icon
}