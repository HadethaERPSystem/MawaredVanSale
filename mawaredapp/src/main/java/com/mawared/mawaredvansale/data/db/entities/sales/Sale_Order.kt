package com.mawared.mawaredvansale.data.db.entities.sales

import android.util.Log
import android.view.ViewDebug
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

const val CURRENT_SALE_ORDER_ID = 0

@Entity
data class Sale_Order (
    var so_clientId: Int?,          // Client Id
    var so_orgId: Int?,             // Organization Id
    var so_no: Int?,               // sale order number
    var so_date: String?,          // sale order date
    var so_refNo: String?,         // sale order reference number
    var so_prefix: String?,
    var so_vo_Id: Int?,
    var so_salesmanId: Int?,       // Salesman Id
    var so_customerId: Int?,       // Customer Id
    var so_ccustomer_name: String?,

    var so_regionId: Int?,         // region Id
    var so_total_amount: Double?,  // total sale amount
    var so_total_discount: Double?,
    var so_net_amount: Double?,    // net sale amount
    var so_currencyId: Int?,       // currency code
    var so_rate: Double?,          // Exchange Rate
    var so_isDeleted: Boolean?,
    var so_latitude: Double?,
    var so_longitude: Double?,
    var so_price_cat_Id: Int?,
    var created_at: String?,       // created datetime
    var created_by: String?,       // created user
    var updated_at: String?,       // Updated datetime
    var updated_by: String?        // Updated user
){
    @PrimaryKey(autoGenerate = false)
    var so_id:  Int = CURRENT_SALE_ORDER_ID
    @Ignore
    var so_org_name: String? = null
    @Ignore
    var so_customer_name: String? = null
    @Ignore
    var so_salesman_name: String? = null
    @Ignore
    var so_cr_symbol: String? = null
    @Ignore
    var so_vo_name: String? = null
    @Ignore
    var so_region_name: String? = null
    var so_price_cat_code: String? = null
    var so_status_code: String? = null
    var so_status_name: String? = null
    @Ignore
    var items: ArrayList<Sale_Order_Items> = arrayListOf()
    @Ignore
    var items_deleted: ArrayList<Sale_Order_Items> = arrayListOf()
}

