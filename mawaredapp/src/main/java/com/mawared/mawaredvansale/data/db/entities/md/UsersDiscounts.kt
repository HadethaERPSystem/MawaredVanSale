package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity

@Entity
data class UsersDiscounts (
    var ud_Id: Int,
    var discPrcnt : Double?
)