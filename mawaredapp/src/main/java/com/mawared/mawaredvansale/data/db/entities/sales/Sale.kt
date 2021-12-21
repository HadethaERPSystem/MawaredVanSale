package com.mawared.mawaredvansale.data.db.entities.sales

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Sale(
    var sl_doc_no: Int?,            // sale number
    var sl_doc_date: String?,// sale date
    var sl_prefix: String?,         // sale prefix
    var sl_refNo: String?,          // sale reference number
    var sl_clientId: Int?,          // client code
    var sl_orgId: Int?,             // organization code
    var sl_vo_Id: Int?,
    var sl_customerId: Int?,        // Customer code
    var sl_salesmanId: Int?,        // Salesman code
    var sl_regionId: Int?,          // region code
    var sl_total_amount: Double?,   // total sale amount
    var sl_total_discount: Double?,   // total sale amount
    var sl_net_amount: Double?,     // net sale amount
    var sl_currencyId: Int?,        // currency code
    var sl_lcurrencyId: Int?,
    var sl_rate: Double?,
    var sl_isDeleted: Boolean?,
    var sl_latitude: Double?,
    var sl_longitude: Double?,
    var sl_price_cat_Id: Int?,
    var sl_paidUSD: Double,
    var sl_changeUSD: Double,
    var sl_paidIQD: Double,
    var sl_changeIQD: Double,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user


) {
    @PrimaryKey(autoGenerate = true)
    var sl_Id:  Int = 0
    var sl_org_name: String? = null
    var sl_org_phone: String? = null
    var sl_customer_name: String? = null
    var sl_salesman_name: String? = null
    var sl_region_name: String? = null
    var sl_cr_symbol: String? = null
    var sl_cr_name: String? = null
    var sl_lcr_symbol: String? = null
    var sl_lcr_name: String? = null
    var sl_vo_name: String? = null
    var sl_vo_code: String? = null
    var sl_contact_name: String? = null
    var sl_customer_phone: String? = null
    var sl_customer_balance: Double? = null
    var sl_price_cat_code: String? = null
    @Ignore
    var items: ArrayList<Sale_Items> = arrayListOf()
    @Ignore
    var items_deleted: ArrayList<Sale_Items> = arrayListOf()
}
