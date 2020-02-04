package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Call_Cycle (
    var cy_customerId: Int?,    // Customer Code
    var cy_salesmanId: Int?,    // Salesman Code
    var cy_routeId: Int?,       // Call Cycle Code
    var cy_latitude: Double?,
    var cy_longitude: Double?,
    var cy_date: String?        // date ... only date without time
){
    @PrimaryKey(autoGenerate = false)
    var cy_id:  Int = 0
    var cy_cu_name: String? = null
    var cy_sm_name: String? = null
    var cy_route_name: String? = null
}