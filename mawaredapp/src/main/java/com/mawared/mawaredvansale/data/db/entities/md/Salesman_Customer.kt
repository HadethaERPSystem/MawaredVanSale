package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Salesman_Customer(
    var sc_clientId: Int?,          // Client Id
    var sc_salesmanId: Int?,        // Salesman Id
    var sc_customerId: Int?,        // Customer Id
    var sc_regionId: Int?,          // Region Id
    var sc_dueday: Int?,            // Payment Terms
    var sc_creditlimit: Double?,    // Customer Credit Limit
    var sc_discount: Double?,       // Customer discount
    var sc_balance: Double?,        // Customer Balance
    var sc_balance_pdc: Double?,    // Post Dated Checks Balance
    var sc_allowCredit: String?     // Allow Credit Limit
) {
    @PrimaryKey(autoGenerate = false)
    var sc_id:  Int = 0
}