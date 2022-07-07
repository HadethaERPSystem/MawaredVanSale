package com.mawared.mawaredvansale.data.db.entities.inventory

import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

class InventoryDoc(
    var vo_Id: Int?,
    var vo_code: String?,
    var docRefno: String?,
    var doc_date: String?,
    var bp_Id: Int?,
    var bp_Name: String?,
    var items_count: Int?,
    var whs_count: Int?,
    var totalPages: Int?,
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var docEntry:  Int = 0

    @Ignore
    var items: ArrayList<InventoryDocLines> = arrayListOf()
}