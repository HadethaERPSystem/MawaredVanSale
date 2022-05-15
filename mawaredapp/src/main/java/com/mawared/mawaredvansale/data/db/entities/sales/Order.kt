package com.mawared.mawaredvansale.data.db.entities.sales

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Order (
    @PrimaryKey
    var od_Id: Int = 0,
    var od_cu_Id: Int?,
    var od_cu_name: String?,
    var od_salesmanId: Int?,
    var cu_phone: String?,
    var cu_price_cat_Id: Int?,
    var payCode: String? = null,
    var cu_price_cat_code: String? = null,
    var cu_price_cat_name: String? = null,
    var cu_credit_limit: Double?,   // Customer Credit Limit
    var cu_credit_days: Int?,
    var cu_credit_age:Int = 0,
    var vo_code : String? = null
)