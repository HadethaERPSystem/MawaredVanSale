package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Customer(
    var cu_code: String?,           // Customer Code
    var cu_cg_Id: Int?,             // Customer Group Id
    var cu_payment_Id: Int?,
    var cu_clientId: Int?,          // Client Id
    var cu_orgId: Int?,             // Department Id or Branch Id
    var cu_barcode: String?,        // Customer Barcode
    var cu_name_ar: String?,        // Arabic customer name
    var cu_name: String?,           // Latin customer name
    var cu_trade_name: String?,
    var cu_address_ar: String?,     // Customer Arabic Address
    var cu_address: String?,        // Customer Latin Address
    var cu_phone: String?,          // Customer Phone
    var cu_mobile: String?,         // Customer Mobile
    var cu_contact_name: String?,    // Contact Name
    var cu_cat_Id: Int?,             // Region Id
    var cu_rg_Id: Int?,             // Region Id
    var cu_cr_Id: Int?,             // Currency Id
    var cu_notes: String?,          // Any Extra Data
    var cu_ref_Id: Int?,             // ERP customer Id
    var cu_balance: Double?,        // Customer Balance
    var cu_credit_limit: Double?,   // Customer Credit Limit
    var cu_credit_days: Int?,
    var cu_payment_terms: String?,   // Payment method : CO: Cash only, CK: Cash And Check, CC: Credit
    var cu_latitude: Double?,
    var cu_longitude: Double?,
    var cu_price_cat_Id: Int?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
): Serializable {
    @PrimaryKey(autoGenerate = false)
    var cu_Id:  Int = 0
    var cu_cg_name: String? = null
    var cu_payment_name: String? = null
    var cu_cat_name: String? = null
    var cu_rg_name: String? = null
    var payCode: String? = null
    var cu_price_cat_code: String? = null
    var cu_price_cat_name: String? = null
    var cu_visit_code: String? = null
    var cu_credit_age:Int = 0
    var cu_cr_code: String? = null
    var totalPages: Int = 0
    var cu_DebitAge: Int? = null
}
