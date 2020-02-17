package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Call_Cycle (
    var cy_cu_Id: Int?,    // Customer Code
    var cy_sm_Id: Int?,    // Salesman Code
    var cy_routeId: Int?,       // Call Cycle Code
    var cy_date: String?,        // date ... only date without time
    var cy_dayname: String?,
    var cy_status_Id: Int?,
    var cy_notes: String?,
    var cy_latitude: Double?,
    var cy_longitude: Double?,
    var created_at: String?,        // created datetime
    var created_by: String?,        // created user
    var updated_at: String?,        // Updated datetime
    var updated_by: String?         // Updated user

){
    @PrimaryKey(autoGenerate = false)
    var cy_id:  Int = 0
    var cy_cu_name: String? = null
    var cy_sm_name: String? = null
    var cy_route_name: String? = null
}