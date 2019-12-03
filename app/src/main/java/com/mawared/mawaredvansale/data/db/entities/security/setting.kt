package com.mawared.mawaredvansale.data.db.entities.security

import androidx.room.PrimaryKey

data class setting(
    var sm_Id: Int?,
    var sm_name: String?,
    var client_Id: Int?,
    var client_name: String?,
    var van_sales_code: String?,
    var printer_name: String?,
    var printer_port: String?
) {
    @PrimaryKey(autoGenerate = false)
    var s_Id: Int = 0
}