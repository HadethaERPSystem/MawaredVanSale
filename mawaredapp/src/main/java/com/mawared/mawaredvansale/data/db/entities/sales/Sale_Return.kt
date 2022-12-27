package com.mawared.mawaredvansale.data.db.entities.sales

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDateTime

@Entity
data class Sale_Return(
    var sr_clientId: Int?,          // Client Id
    var sr_orgId: Int?,             // Organization Id
    var sr_doc_no: Int?,            // Return Document Number
    var sr_doc_date: String?,// Return Document Date
    var sr_refno: String?,          // Return Reference Number
    var sr_prefix: String?,
    var sr_vo_Id: Int?,
    var sr_salesmanId: Int?,        // Salesman Id
    var sr_customerId: Int?,        // Customer Id
    var sr_regionId: Int?,          // region code
    var sr_total_amount: Double?,   // total sale amount
    var sr_total_disc: Double?,
    var sr_net_amount: Double?,     // net sale amount
    var sr_discPrcnt: Double,
    var sr_currencyId: Int?,        // Currency code
    var sr_lcurrencyId: Int?,
    var sr_rate: Double?,           // Current Exchange Rate
    var sr_ret_ref_Id : Int?,           // Reference Invoice Id
    var sr_ret_ref_no: String?,
    var sr_isDeleted: Boolean?,
    var sr_statusId: Int?,
    var sr_latitude: Double?,
    var sr_longitude: Double?,
    var sr_price_cat_Id: Int?,

    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user
) {
    @PrimaryKey(autoGenerate = true)
    var sr_Id: Int = 0
    var sr_org_name: String? = null
    var sr_org_phone: String? = null
    var sr_vo_name: String? = null
    var sr_vo_code: String? = null
    var sr_salesman_name: String? = null
    var sr_customer_name: String? = null
    var sr_region_name: String? = null
    var sr_cr_symbol: String? = null
    var sr_cr_name: String? = null
    var sr_lcr_symbol: String? = null
    var sr_lcr_name: String? = null
    var sr_ret_refNo: String? = null
    var sr_status_name: String? = null
    var sr_status_code: String? = null
    var sr_contact_name: String? = null
    var sr_customer_phone: String? = null
    var sr_price_cat_code: String? = null
    @Ignore
    var items: ArrayList<Sale_Return_Items> = arrayListOf()
    @Ignore
    var items_deleted: ArrayList<Sale_Return_Items> = arrayListOf()
}
