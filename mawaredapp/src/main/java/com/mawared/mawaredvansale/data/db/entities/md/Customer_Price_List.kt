package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Customer_Price_List (
    var cpl_code: String?,      // Customer Price List Code
    var cpl_customerId: Int?,   // Customer Id
    var cpl_salesmanId: Int?,   // Salesman Id
    var cpl_clientId: Int?,     // Client Id
    var cpl_currencyId: Int     // Currency Id
){
    @PrimaryKey(autoGenerate = false)
    var cpl_id:  Int = 0
}