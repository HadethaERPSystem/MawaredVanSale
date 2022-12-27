package com.mawared.mawaredvansale.data.db.entities.security

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Menu (
    var order:Int?,
    var menu_name: String,
    var menu_code: String,
    val icon: String,
    var permission: String
){
    @PrimaryKey(autoGenerate = false)
    var menu_id: Int = 0
}