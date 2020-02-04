package com.mawared.mawaredvansale.data.db.entities.md

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Product (
    var pr_code: String?,             // Product Name
    var pr_barcode: String?,          //Product barcode
    var pr_description: String?,      //Product Latin Name
    var pr_description_ar: String?,   //Product Arabic Name
    var pr_clientId: Int?,           //Client Id
    var pr_categoryId: Int?,          //Product Category
    var pr_brandId: Int?,             //Product Brand Name
    var pr_vat: Double?,              //Porduct Value Added Tax
    var pr_uom_Id: Int?,               //Unit of measurement
    var pr_image_name: String?,
    var pr_qty: Double?,
    var pr_unit_price: Double?,
    var pr_batch_no: String?,
    var pr_expiry_date: String?,
    var pr_mfg_date: String?,
    var pr_wr_Id: Int?,
    var pr_prc_cat_Id: Int?
){
    @PrimaryKey(autoGenerate = false)
    var pr_Id:  Int = 0
    var pr_wr_name: String? = null
}