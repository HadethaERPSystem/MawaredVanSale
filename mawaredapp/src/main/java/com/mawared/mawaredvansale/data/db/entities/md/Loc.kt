package com.mawared.mawaredvansale.data.db.entities.md

import com.mawared.mawaredvansale.data.db.entities.inventory.InventoryDocLines

class Loc (
    var loc_Id: Int?,
    var loc_name: String?,
    var qty : Double?
    ){
    var docLine: InventoryDocLines? = null
    var addQty: Double? = null
}