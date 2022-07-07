package com.mawared.mawaredvansale.data.db.entities.inventory

import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.mawared.mawaredvansale.data.db.entities.md.Loc

class InventoryDocLines(
    var lineNum: Int?,
    var docEntry: Int?,
    var prodId: Int?,
    var prod_name: String?,
    var barcode: String?,
    var uomEntry: Int?,
    var uomName: String?,
    var qty: Double?,
    var uomSize: Double?,
    var invQty: Double?,
    var isGift: Boolean?,
    var unitCost: Double?,
    var batchNo: String?,
    var expr_date: String?,
    var mfg_date: String?,
    var itemLocations: String?,
) {
    @PrimaryKey(autoGenerate = true)
    var docLineEntry: Int = 0

    @Ignore
    var itemLoc: ArrayList<Loc>? = arrayListOf()
    @Ignore
    var itemSelectedLoc: ArrayList<Loc>? = arrayListOf()
}