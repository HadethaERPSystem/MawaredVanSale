package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Salesman (
    var sm_user_id: Int?,
    var sm_code: String?,
    var sm_name: String?,
    var sm_name_ar: String?,
    var sm_login: String?,
    var sm_password: String?,
    var sm_warehouse_code: String?,
    var sm_warehouse_id: Int?,
    var sm_credit_limit: Double?,
    var sm_balance: Double?,
    var sm_profile: String?
){
    @PrimaryKey(autoGenerate = false)
    var sm_id:  Int = 0
}