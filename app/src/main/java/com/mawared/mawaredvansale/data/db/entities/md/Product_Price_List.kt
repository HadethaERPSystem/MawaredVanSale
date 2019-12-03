package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate

@Entity
data class Product_Price_List(
    var pl_prod_Id: Int?,      //Product Identifier
    var pl_currencyId: Int?,    // CurrencyId
    var pl_clientId: Int?,      // Client Identifier
    var pl_unitPirce: Double?,  // Product Unit Price
    var pl_uom_Id: Int?        // Unit of measurement
) {
    @PrimaryKey(autoGenerate = false)
    var pl_Id:  Int = 0
}