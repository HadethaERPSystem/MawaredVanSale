package com.mawared.mawaredvansale.data.db.entities.sales

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDateTime

@Entity
data class Due_Invoice(
    var di_customerId: Int?,    // Customer Id
    var di_salesmanId: Int?,    // Salesman Id
    var di_regionId: Int?,      // Region Id
    var di_currencyId: Int?,    // Currency Id
    var di_clientId: Int?,      // Client Id
    var di_invoiceId: Int?,     // Invoice Reference Id
    var di_refno: String?,      // Invoice Referecne Number
    var di_date: String?,// Issue Date
    var di_due_date: String?,// Due Date
    var di_total_amount: Double?,   // Transaction Amount
    var di_remaining_Amount: Double?// Remaining Amount
) {
    @PrimaryKey(autoGenerate = false)
    var di_Id:  Int = 0
}